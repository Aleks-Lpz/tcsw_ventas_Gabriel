package com.ventas.adapter.out.jdbc;

import com.ventas.model.Producto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JdbcProductoRepositoryIT {

    private static DataSource dataSource;
    private JdbcProductoRepository repositorio;

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
    void limpiarYSembrar() throws Exception {
        repositorio = new JdbcProductoRepository(dataSource);
        
        // TRUNCATE en cascada garantiza que no haya conflictos de llaves foráneas con partidas
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("TRUNCATE partida, venta, producto RESTART IDENTITY CASCADE");
        }
    }

    @Test
    void guardaUnProductoYLoRecuperaCorrectamente() {
        Producto nuevo = new Producto("IT-001", "Monitor Integración", 3500.50, 15);
        
        Producto guardado = repositorio.guardar(nuevo);
        assertSame(nuevo, guardado); 
        
        List<Producto> productos = repositorio.obtenerTodos();
        assertEquals(1, productos.size());
        assertEquals("IT-001", productos.get(0).getCodigo());
        assertEquals("Monitor Integración", productos.get(0).getNombre());
        assertEquals(3500.50, productos.get(0).getPrecio());
        assertEquals(15, productos.get(0).getExistencia());
    }

    @Test
    void obtenerTodosRetornaMultiplesProductos() {
        repositorio.guardar(new Producto("IT-001", "Mouse", 250.0, 10));
        repositorio.guardar(new Producto("IT-002", "Teclado", 500.0, 5));
        repositorio.guardar(new Producto("IT-003", "Audífonos", 800.0, 8));

        List<Producto> productos = repositorio.obtenerTodos();
        
        assertEquals(3, productos.size());
    }

    @Test
    void guardarProductoConCodigoDuplicadoLanzaPersistenciaException() {
        Producto original = new Producto("IT-DUP", "Original", 100.0, 10);
        Producto clon = new Producto("IT-DUP", "Clon Inválido", 50.0, 5); 

        repositorio.guardar(original);
        
        PersistenciaException error = assertThrows(PersistenciaException.class, 
            () -> repositorio.guardar(clon));
        
        assertTrue(error.getMessage().contains("Causa"));
    }

    @Test
    void guardarProductoNuloLanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
    }
}