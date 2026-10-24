package com.ventas.adapter.out.jdbc;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Partida;
import com.ventas.model.Venta;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Adaptador de salida que persiste ventas en PostgreSQL mediante JDBC.
 * <p>
 * Implementa el puerto {@link VentaRepository} sin modificarlo, de modo que el
 * nucleo sigue sin conocer java.sql. La venta y todas sus partidas se guardan en
 * una sola transaccion: si cualquier INSERT falla (producto inexistente, cantidad
 * invalida, etc.) se hace rollback y no queda ninguna fila a medias.
 * Todas las sentencias usan PreparedStatement con parametros.
 */
public class JdbcVentaRepository implements VentaRepository {

    static final String INSERT_VENTA =
            "INSERT INTO venta (descuento, total) VALUES (?, ?) RETURNING id";

    static final String INSERT_PARTIDA =
            "INSERT INTO partida (venta_id, producto_codigo, cantidad, precio_unitario) "
                    + "VALUES (?, ?, ?, ?)";

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
