package com.ventas.adapter.out.jdbc;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.ExistenciaInsuficienteException;
import com.ventas.model.Partida;
import com.ventas.model.Venta;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Adaptador de salida que persiste ventas en PostgreSQL mediante JDBC.
 * <p>
 * Implementa el puerto {@link VentaRepository} sin modificarlo, de modo que el
 * nucleo sigue sin conocer java.sql. La operacion completa (descontar existencias,
 * insertar la venta e insertar sus partidas) ocurre en una sola transaccion: si
 * cualquier paso falla (existencia insuficiente, producto inexistente, cantidad
 * invalida, etc.) se hace rollback y no queda ninguna fila a medias ni inventario
 * movido a la mitad. Todas las sentencias usan PreparedStatement con parametros.
 */
public class JdbcVentaRepository implements VentaRepository {

    static final String INSERT_VENTA =
            "INSERT INTO venta (descuento, total) VALUES (?, ?) RETURNING id";

    static final String INSERT_PARTIDA =
            "INSERT INTO partida (venta_id, producto_codigo, cantidad, precio_unitario) "
                    + "VALUES (?, ?, ?, ?)";

    /**
     * Descuenta y devuelve la existencia resultante en una sola sentencia. La
     * condicion {@code existencia >= ?} hace que la propia base decida si hay
     * stock, asi que dos ventas concurrentes no pueden pasar ambas la validacion:
     * la segunda espera el lock del renglon y luego no actualiza ninguna fila.
     */
    static final String UPDATE_EXISTENCIA =
            "UPDATE producto SET existencia = existencia - ? "
                    + "WHERE codigo = ? AND existencia >= ? RETURNING existencia";

    static final String SELECT_EXISTENCIA =
            "SELECT existencia FROM producto WHERE codigo = ?";

    private final DataSource dataSource;

    public JdbcVentaRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "El DataSource no puede ser nulo.");
    }

    @Override
    public Venta guardar(Venta venta) {
        insertar(venta);
        return venta;
    }

    /**
     * Inserta la venta con sus partidas en una transaccion y devuelve el id generado.
     */
    public long insertar(Venta venta) {
        validar(venta);

        try (Connection conexion = dataSource.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try {
                descontarExistencias(conexion, venta);
                long ventaId = insertarVenta(conexion, venta);
                insertarPartidas(conexion, ventaId, venta);
                conexion.commit();
                return ventaId;
            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudo guardar la venta; la transaccion se revirtio. Causa: " + causaRaiz(e), e);
        }
    }

    /** En un batch, PostgreSQL deja el detalle real en getNextException(). */
    private static String causaRaiz(SQLException e) {
        SQLException siguiente = e.getNextException();
        return siguiente != null ? siguiente.getMessage() : e.getMessage();
    }

    private void validar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        if (venta.getPartidas().isEmpty()) {
            throw new IllegalArgumentException("Una venta sin partidas no puede persistirse.");
        }
    }

    /**
     * Descuenta del catalogo las unidades vendidas, dentro de la transaccion de la
     * venta. Los productos se recorren ordenados por codigo para que todas las
     * ventas tomen los locks en el mismo orden y no se produzcan interbloqueos.
     *
     * @throws ExistenciaInsuficienteException si algun producto no alcanza; el
     *         llamador revierte la transaccion y el inventario queda intacto.
     */
    private void descontarExistencias(Connection conexion, Venta venta) throws SQLException {
        Map<String, Integer> unidades = unidadesPorProducto(venta);

        try (PreparedStatement ps = conexion.prepareStatement(UPDATE_EXISTENCIA)) {
            for (Map.Entry<String, Integer> entrada : unidades.entrySet()) {
                String codigo = entrada.getKey();
                int cantidad = entrada.getValue();

                ps.setInt(1, cantidad);
                ps.setString(2, codigo);
                ps.setInt(3, cantidad);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        // No se actualizo ningun renglon: o el producto no existe o no alcanza.
                        throw sinExistencia(conexion, codigo, cantidad);
                    }
                }
            }
        }
    }

    /**
     * Agrupa las unidades por codigo de producto para descontar una sola vez
     * cuando la venta trae varias partidas del mismo articulo. El TreeMap fija el
     * orden de los UPDATE y evita interbloqueos entre transacciones.
     */
    private static Map<String, Integer> unidadesPorProducto(Venta venta) {
        Map<String, Integer> unidades = new TreeMap<>();
        for (Partida partida : venta.getPartidas()) {
            unidades.merge(partida.getProducto().getCodigo(), partida.getCantidad(), Integer::sum);
        }
        return unidades;
    }

    /**
     * Distingue las dos causas de un UPDATE que no afecto renglones: producto
     * ausente (error de integridad) o stock insuficiente (regla de negocio).
     */
    private RuntimeException sinExistencia(Connection conexion, String codigo, int solicitado)
            throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(SELECT_EXISTENCIA)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El producto '" + codigo + "' no existe en el catalogo.");
                }
                return new ExistenciaInsuficienteException(codigo, solicitado, rs.getInt(1));
            }
        }
    }

    private long insertarVenta(Connection conexion, Venta venta) throws SQLException {
        BigDecimal total = dinero(venta.calcularTotal());
        BigDecimal subtotal = dinero(venta.getPartidas().stream()
                .mapToDouble(Partida::getSubtotal)
                .sum());
        BigDecimal descuento = subtotal.subtract(total).max(BigDecimal.ZERO);

        try (PreparedStatement ps = conexion.prepareStatement(INSERT_VENTA)) {
            ps.setBigDecimal(1, descuento);
            ps.setBigDecimal(2, total);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("La base de datos no devolvio el id de la venta.");
                }
                return rs.getLong(1);
            }
        }
    }

    private void insertarPartidas(Connection conexion, long ventaId, Venta venta) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(INSERT_PARTIDA)) {
            for (Partida partida : venta.getPartidas()) {
                ps.setLong(1, ventaId);
                ps.setString(2, partida.getProducto().getCodigo());
                ps.setInt(3, partida.getCantidad());
                ps.setBigDecimal(4, dinero(partida.getProducto().getPrecio()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }
}
