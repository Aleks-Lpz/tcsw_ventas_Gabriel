package com.ventas.adapter.out.jdbc;

import com.ventas.model.ExistenciaInsuficienteException;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integracion contra PostgreSQL 16 real (docker compose).
 * Se ejecutan con `mvn verify` y requieren DB_URL, DB_USER y DB_PASSWORD.
 * Si no estan definidas, las pruebas se marcan como omitidas (no fallan).
 * Solo se usan datos sinteticos.
 */
class JdbcVentaRepositoryIT {

    private static DataSource dataSource;
    private JdbcVentaRepository repositorio;

    private final Producto cuaderno = new Producto("SIN-001", "Cuaderno sintetico", 25.50, 100);
    private final Producto lapiz = new Producto("SIN-002", "Lapiz sintetico", 8.00, 100);

    @BeforeAll
    static void prepararBase() throws Exception {
        ConfiguracionJdbc configuracion = ConfiguracionJdbc.desdeEntorno().orElse(null);
        assumeTrue(configuracion != null, "Define DB_URL, DB_USER y DB_PASSWORD para ejecutar las pruebas de integracion");
        dataSource = configuracion.crearDataSource();

        String schema = Files.readString(Paths.get("db/schema.sql"));
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute(schema);
        }
    }

    @BeforeEach
    void limpiarYSembrar() throws SQLException {
        repositorio = new JdbcVentaRepository(dataSource);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("TRUNCATE partida, venta, producto RESTART IDENTITY CASCADE");
        }
        insertarProducto(cuaderno);
        insertarProducto(lapiz);
    }

    @Test
    void guardaLaVentaConSusPartidasYSePuedeRecuperar() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 2));
        venta.agregarPartida(new Partida(lapiz, 3));

        long id = repositorio.insertar(venta);

        assertEquals(new BigDecimal("75.00"), consultarDecimal("SELECT total FROM venta WHERE id = ?", id));
        assertEquals(new BigDecimal("0.00"), consultarDecimal("SELECT descuento FROM venta WHERE id = ?", id));
        assertEquals(2, contar("SELECT count(*) FROM partida WHERE venta_id = ?", id));
        assertEquals(2, contar(
                "SELECT cantidad FROM partida WHERE venta_id = ? AND producto_codigo = 'SIN-001'", id));
    }

    @Test
    void guardarDevuelveLaMismaVentaComoIndicaElPuerto() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(lapiz, 1));

        assertSame(venta, repositorio.guardar(venta));
        assertEquals(1, contarTabla("venta"));
    }

    @Test
    void guardaElDescuentoYElTotalNeto() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 4));
        venta.aplicarDescuento(12.0);

        long id = repositorio.insertar(venta);

        assertEquals(new BigDecimal("12.00"), consultarDecimal("SELECT descuento FROM venta WHERE id = ?", id));
        assertEquals(new BigDecimal("90.00"), consultarDecimal("SELECT total FROM venta WHERE id = ?", id));
    }

    @Test
    void siUnaPartidaTieneProductoInexistenteSeHaceRollbackDeTodo() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 1));
        venta.agregarPartida(new Partida(new Producto("NO-EXISTE", "Fantasma", 10.0, 1), 1));

        PersistenciaException error = assertThrows(PersistenciaException.class, () -> repositorio.guardar(venta));

        assertTrue(error.getMessage().contains("revirtio"));
        assertEquals(0, contarTabla("venta"), "No debe quedar la venta a medias");
        assertEquals(0, contarTabla("partida"), "No debe quedar ninguna partida");
        assertEquals(100, existenciaDe("SIN-001"), "Tampoco debe quedar inventario descontado");
    }

    @Test
    void laBaseRechazaUnaCantidadInvalidaYRevierteLaTransaccion() throws SQLException {
        Partida partida = new Partida(cuaderno, 1);
        partida.setCantidad(0); // el modelo lo permite despues de construir; la base no
        Venta venta = new Venta();
        venta.agregarPartida(partida);

        assertThrows(PersistenciaException.class, () -> repositorio.guardar(venta));

        assertEquals(0, contarTabla("venta"));
        assertEquals(0, contarTabla("partida"));
    }

    @Test
    void unCodigoMaliciosoSeTrataComoDatoYNoComoSql() throws SQLException {
        Producto malicioso = new Producto("x'); DROP TABLE venta; --", "Inyeccion", 1.0, 1);
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(malicioso, 1));

        assertThrows(PersistenciaException.class, () -> repositorio.guardar(venta));

        // Si el texto se hubiera ejecutado como SQL, la tabla ya no existiria.
        assertEquals(0, contarTabla("venta"));
    }

    @Test
    void despuesDeUnRollbackElRepositorioSigueFuncionando() throws SQLException {
        Venta invalida = new Venta();
        invalida.agregarPartida(new Partida(new Producto("NO-EXISTE", "Fantasma", 10.0, 1), 1));
        assertThrows(PersistenciaException.class, () -> repositorio.guardar(invalida));

        Venta valida = new Venta();
        valida.agregarPartida(new Partida(lapiz, 2));
        repositorio.guardar(valida);

        assertEquals(1, contarTabla("venta"));
        assertEquals(1, contarTabla("partida"));
    }


    // ---- C02 / issue #40: consistencia transaccional del inventario ----

    @Test
    void laVentaDescuentaLaExistenciaDelCatalogoEnLaMismaTransaccion() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 7));
        venta.agregarPartida(new Partida(lapiz, 2));

        repositorio.insertar(venta);

        assertEquals(93, existenciaDe("SIN-001"), "El cuaderno debe bajar de 100 a 93");
        assertEquals(98, existenciaDe("SIN-002"), "El lapiz debe bajar de 100 a 98");
    }

    @Test
    void variasPartidasDelMismoProductoDescuentanUnaSolaVezElTotal() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 4));
        venta.agregarPartida(new Partida(cuaderno, 6));

        repositorio.insertar(venta);

        assertEquals(90, existenciaDe("SIN-001"));
        assertEquals(2, contarTabla("partida"), "Las dos partidas se conservan como documento");
    }

    @Test
    void unaVentaQueAgotaLaExistenciaExactaSeAceptaYDejaElCatalogoEnCero() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 100));

        repositorio.insertar(venta);

        assertEquals(0, existenciaDe("SIN-001"), "El limite superior permitido es la existencia completa");
        assertEquals(1, contarTabla("venta"));
    }

    @Test
    void unaVentaQueExcedeLaExistenciaSeRechazaYNoDejaRastro() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(lapiz, 3));
        venta.agregarPartida(new Partida(cuaderno, 101)); // 101 > 100 disponibles

        ExistenciaInsuficienteException error =
                assertThrows(ExistenciaInsuficienteException.class, () -> repositorio.guardar(venta));

        assertEquals("SIN-001", error.getCodigo());
        assertEquals(101, error.getSolicitado());
        assertEquals(100, error.getDisponible());
        assertEquals(0, contarTabla("venta"), "No debe quedar la venta");
        assertEquals(0, contarTabla("partida"), "No debe quedar ninguna partida");
        assertEquals(100, existenciaDe("SIN-001"), "La existencia del producto que fallo queda intacta");
        assertEquals(100, existenciaDe("SIN-002"), "El descuento del producto anterior tambien se revierte");
    }

    @Test
    void laSobreventaEsUnErrorDeNegocioYNoDeInfraestructura() {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 1000));

        ExistenciaInsuficienteException error =
                assertThrows(ExistenciaInsuficienteException.class, () -> repositorio.guardar(venta));

        assertFalse(PersistenciaException.class.isInstance(error),
                "La regla de negocio debe distinguirse de un fallo de la base");
        assertTrue(error.getMessage().contains("Existencia insuficiente"));
    }

    @Test
    void unRollbackPorIntegridadTampocoMueveElInventario() throws SQLException {
        Partida partida = new Partida(cuaderno, 5);
        partida.setCantidad(0); // el modelo lo permite despues de construir; el CHECK de la base no
        Venta venta = new Venta();
        venta.agregarPartida(partida);

        assertThrows(PersistenciaException.class, () -> repositorio.guardar(venta));

        assertEquals(100, existenciaDe("SIN-001"));
    }

    @Test
    void despuesDeUnaSobreventaRevertidaLaSiguienteVentaValidaSiDescuenta() throws SQLException {
        Venta excedida = new Venta();
        excedida.agregarPartida(new Partida(cuaderno, 500));
        assertThrows(ExistenciaInsuficienteException.class, () -> repositorio.guardar(excedida));

        Venta valida = new Venta();
        valida.agregarPartida(new Partida(cuaderno, 10));
        repositorio.guardar(valida);

        assertEquals(90, existenciaDe("SIN-001"));
        assertEquals(1, contarTabla("venta"));
    }

    // ---- utilidades de verificacion (consultas parametrizadas) ----

    private void insertarProducto(Producto p) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO producto (codigo, nombre, precio, existencia) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, BigDecimal.valueOf(p.getPrecio()));
            ps.setInt(4, p.getExistencia());
            ps.executeUpdate();
        }
    }

    private int existenciaDe(String codigo) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT existencia FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "No se encontro el producto " + codigo);
                return rs.getInt(1);
            }
        }
    }

    private BigDecimal consultarDecimal(String sql, long id) throws SQLException {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBigDecimal(1);
            }
        }
    }

    private int contar(String sql, long id) throws SQLException {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int contarTabla(String tabla) throws SQLException {
        // El nombre de la tabla viene de las propias pruebas, nunca de una entrada externa.
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
