package com.ventas.adapter.out.hibernate;

import com.ventas.adapter.out.jdbc.ConfiguracionJdbc;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integracion para los adaptadores de Hibernate contra PostgreSQL 16 real.
 * Se ejecutan con `mvn verify` y requieren DB_URL, DB_USER y DB_PASSWORD.
 */
class HibernateVentaRepositoryIT {

    private static DataSource dataSource;
    private static EntityManagerFactory entityManagerFactory;

    private HibernateVentaRepository ventaRepositorio;
    private HibernateProductoRepository productoRepositorio;

    private final Producto cuaderno = new Producto("SIN-001", "Cuaderno sintetico", 25.50, 100);
    private final Producto lapiz = new Producto("SIN-002", "Lapiz sintetico", 8.00, 100);

    @BeforeAll
    static void prepararBase() throws Exception {
        ConfiguracionJdbc configJdbc = ConfiguracionJdbc.desdeEntorno().orElse(null);
        ConfiguracionHibernate configHibernate = ConfiguracionHibernate.desdeEntorno().orElse(null);
        assumeTrue(configJdbc != null && configHibernate != null,
                "Define DB_URL, DB_USER y DB_PASSWORD para ejecutar las pruebas de integracion");

        dataSource = configJdbc.crearDataSource();
        String schema = Files.readString(Paths.get("db/schema.sql"));
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute(schema);
        }
        entityManagerFactory = configHibernate.crearEntityManagerFactory();
    }

    @AfterAll
    static void cerrarFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void limpiarYSembrar() throws SQLException {
        ventaRepositorio = new HibernateVentaRepository(entityManagerFactory);
        productoRepositorio = new HibernateProductoRepository(entityManagerFactory);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("TRUNCATE partida, venta, producto RESTART IDENTITY CASCADE");
        }
        productoRepositorio.guardar(cuaderno);
        productoRepositorio.guardar(lapiz);
    }

    @Test
    void guardaLaVentaConDosPartidasYLaReleeCorrectamente() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 2));
        venta.agregarPartida(new Partida(lapiz, 3));

        long id = ventaRepositorio.insertar(venta);

        assertEquals(new BigDecimal("75.00"), consultarDecimal("SELECT total FROM venta WHERE id = ?", id));
        assertEquals(new BigDecimal("0.00"), consultarDecimal("SELECT descuento FROM venta WHERE id = ?", id));
        assertEquals(2, contar("SELECT count(*) FROM partida WHERE venta_id = ?", id));
        assertEquals(2, contar(
                "SELECT cantidad FROM partida WHERE venta_id = ? AND producto_codigo = 'SIN-001'", id));

        // Releer la venta con un EntityManager nuevo para verificar el mapeo inverso y la persistencia real
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            VentaEntity releidaEntity = em.find(VentaEntity.class, id);
            assertNotNull(releidaEntity);
            Venta releida = VentaMapper.toModelVenta(releidaEntity);
            assertEquals(2, releida.getPartidas().size());
            assertEquals(75.00, releida.calcularTotal());
        } finally {
            em.close();
        }
    }

    @Test
    void guardarDevuelveLaMismaVentaComoIndicaElPuerto() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(lapiz, 1));

        assertSame(venta, ventaRepositorio.guardar(venta));
        assertEquals(1, contarTabla("venta"));
    }

    @Test
    void guardaElDescuentoYElTotalNeto() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 4));
        venta.aplicarDescuento(12.0);

        long id = ventaRepositorio.insertar(venta);

        assertEquals(new BigDecimal("12.00"), consultarDecimal("SELECT descuento FROM venta WHERE id = ?", id));
        assertEquals(new BigDecimal("90.00"), consultarDecimal("SELECT total FROM venta WHERE id = ?", id));
    }

    @Test
    void siUnaPartidaTieneProductoInexistenteSeHaceRollbackDeTodo() throws SQLException {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 1));
        venta.agregarPartida(new Partida(new Producto("NO-EXISTE", "Fantasma", 10.0, 1), 1));

        assertThrows(RuntimeException.class, () -> ventaRepositorio.guardar(venta));

        assertEquals(0, contarTabla("venta"), "No debe quedar la venta a medias");
        assertEquals(0, contarTabla("partida"), "No debe quedar ninguna partida");
    }

    @Test
    void despuesDeUnRollbackElRepositorioSigueFuncionando() throws SQLException {
        Venta invalida = new Venta();
        invalida.agregarPartida(new Partida(new Producto("NO-EXISTE", "Fantasma", 10.0, 1), 1));
        assertThrows(RuntimeException.class, () -> ventaRepositorio.guardar(invalida));

        Venta valida = new Venta();
        valida.agregarPartida(new Partida(lapiz, 2));
        ventaRepositorio.guardar(valida);

        assertEquals(1, contarTabla("venta"));
        assertEquals(1, contarTabla("partida"));
    }

    @Test
    void productoRepositorioGuardaYConsultaTodosContraPostgreSQL() {
        Producto nuevo = new Producto("SIN-003", "Borrador sintetico", 15.556, 40);
        productoRepositorio.guardar(nuevo);

        List<Producto> productos = productoRepositorio.obtenerTodos();
        assertEquals(3, productos.size());
        assertTrue(productos.stream().anyMatch(
                p -> p.getCodigo().equals("SIN-003") && p.getPrecio() == 15.56));
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
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}