package com.ventas.adapter.out.jdbc;

import com.ventas.model.ExistenciaInsuficienteException;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
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
        assertTrue(JdbcVentaRepository.UPDATE_EXISTENCIA.contains("codigo = ?"));
        assertTrue(JdbcVentaRepository.SELECT_EXISTENCIA.contains("codigo = ?"));
    }

    @Test
    void elDescuentoDeExistenciaLoDecideLaBaseYNoElAdaptador() {
        // La condicion viaja en el WHERE: si el renglon no cumple, no se actualiza
        // nadie y el adaptador se entera por el numero de filas, no por una lectura
        // previa que otra transaccion pudiera invalidar.
        assertTrue(JdbcVentaRepository.UPDATE_EXISTENCIA.contains("existencia = existencia - ?"));
        assertTrue(JdbcVentaRepository.UPDATE_EXISTENCIA.contains("existencia >= ?"));
        assertTrue(JdbcVentaRepository.UPDATE_EXISTENCIA.contains("RETURNING existencia"));
    }

    @Test
    void variasPartidasDelMismoProductoSeConsolidanEnUnSoloDescuento() throws Exception {
        Producto cuaderno = new Producto("SIN-001", "Cuaderno sintetico", 25.50, 100);
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(cuaderno, 2));
        venta.agregarPartida(new Partida(cuaderno, 3));
        venta.agregarPartida(new Partida(new Producto("SIN-002", "Lapiz", 8.0, 10), 1));

        Map<String, Integer> unidades = invocarUnidadesPorProducto(venta);

        assertEquals(2, unidades.size());
        assertEquals(5, unidades.get("SIN-001"));
        assertEquals(1, unidades.get("SIN-002"));
    }

    @Test
    void losDescuentosSeOrdenanPorCodigoParaEvitarInterbloqueos() throws Exception {
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(new Producto("SIN-009", "Ultimo", 5.0, 10), 1));
        venta.agregarPartida(new Partida(new Producto("SIN-001", "Primero", 5.0, 10), 1));
        venta.agregarPartida(new Partida(new Producto("SIN-005", "Medio", 5.0, 10), 1));

        assertEquals(List.of("SIN-001", "SIN-005", "SIN-009"),
                List.copyOf(invocarUnidadesPorProducto(venta).keySet()));
    }

    @Test
    void laExcepcionDeNegocioInformaSolicitadoYDisponible() {
        ExistenciaInsuficienteException error = new ExistenciaInsuficienteException("SIN-001", 50, 40);

        assertEquals("SIN-001", error.getCodigo());
        assertEquals(50, error.getSolicitado());
        assertEquals(40, error.getDisponible());
        assertTrue(error.getMessage().contains("Solicitado: 50"));
        assertTrue(error.getMessage().contains("disponible: 40"));
        assertTrue(error.getMessage().contains("no se registro"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> invocarUnidadesPorProducto(Venta venta) throws Exception {
        Method metodo = JdbcVentaRepository.class.getDeclaredMethod("unidadesPorProducto", Venta.class);
        metodo.setAccessible(true);
        return (Map<String, Integer>) metodo.invoke(null, venta);
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
