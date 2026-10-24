package com.ventas.adapter.out.jdbc;

import com.ventas.model.Venta;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del adaptador JDBC que no necesitan base de datos.
 * Verifican que los estados invalidos se rechazan antes de abrir una conexion.
 */
class JdbcVentaRepositoryTest {

    @Test
    void noSePuedeCrearSinDataSource() {
        assertThrows(NullPointerException.class, () -> new JdbcVentaRepository(null));
    }

    @Test
    void unaVentaNullSeRechazaSinAbrirConexion() {
        AtomicInteger conexiones = new AtomicInteger();
        JdbcVentaRepository repositorio = new JdbcVentaRepository(dataSourceQueCuenta(conexiones));

        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
        assertEquals(0, conexiones.get());
    }

    @Test
    void unaVentaSinPartidasSeRechazaSinAbrirConexion() {
        AtomicInteger conexiones = new AtomicInteger();
        JdbcVentaRepository repositorio = new JdbcVentaRepository(dataSourceQueCuenta(conexiones));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(new Venta()));
        assertTrue(error.getMessage().contains("sin partidas"));
        assertEquals(0, conexiones.get());
    }

    @Test
    void lasSentenciasSonParametrizadas() {
        assertTrue(JdbcVentaRepository.INSERT_VENTA.contains("VALUES (?, ?)"));
        assertTrue(JdbcVentaRepository.INSERT_PARTIDA.contains("VALUES (?, ?, ?, ?)"));
    }

    @Test
    void laConfiguracionEsVaciaSiFaltaAlgunaVariable() {
        assertTrue(ConfiguracionJdbc.desde(Map.of()).isEmpty());
        assertTrue(ConfiguracionJdbc.desde(Map.of("DB_URL", "jdbc:postgresql://localhost/x")).isEmpty());
    }

    @Test
    void laConfiguracionNuncaMuestraElPassword() {
        ConfiguracionJdbc configuracion = ConfiguracionJdbc.desde(Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/ventas",
                "DB_USER", "ventas_app",
                "DB_PASSWORD", "secreto-sintetico")).orElseThrow();

        assertFalse(configuracion.toString().contains("secreto-sintetico"));
        assertTrue(configuracion.toString().contains("***"));
    }

    private static DataSource dataSourceQueCuenta(AtomicInteger contador) {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("getConnection")) {
                        contador.incrementAndGet();
                    }
                    throw new UnsupportedOperationException("No se esperaba usar la base de datos");
                });
    }
}
