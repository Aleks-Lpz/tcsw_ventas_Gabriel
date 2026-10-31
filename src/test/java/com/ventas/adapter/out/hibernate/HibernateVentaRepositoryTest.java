package com.ventas.adapter.out.hibernate;

import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HibernateVentaRepositoryTest {

    @Test
    void noSePuedeCrearSinEntityManagerFactory() {
        assertThrows(NullPointerException.class, () -> new HibernateVentaRepository(null));
    }

    @Test
    void unaVentaNullSeRechazaSinAbrirEntityManager() {
        AtomicInteger aperturas = new AtomicInteger();
        HibernateVentaRepository repositorio = new HibernateVentaRepository(emfQueCuenta(aperturas));

        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
        assertEquals(0, aperturas.get());
    }

    @Test
    void unaVentaSinPartidasSeRechazaSinAbrirEntityManager() {
        AtomicInteger aperturas = new AtomicInteger();
        HibernateVentaRepository repositorio = new HibernateVentaRepository(emfQueCuenta(aperturas));

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(new Venta()));
        assertTrue(error.getMessage().contains("sin partidas"));
        assertEquals(0, aperturas.get());
    }

    @Test
    void guardarPersisteEnUnaTransaccionYCierraElEntityManager() {
        AtomicBoolean iniciada = new AtomicBoolean(false);
        AtomicBoolean confirmada = new AtomicBoolean(false);
        AtomicBoolean cerrado = new AtomicBoolean(false);
        AtomicBoolean usoGetReference = new AtomicBoolean(false);

        EntityManagerFactory emf = crearEmfSimulado(iniciada, confirmada, new AtomicBoolean(), cerrado, usoGetReference, false);
        HibernateVentaRepository repositorio = new HibernateVentaRepository(emf);

        Producto producto = new Producto("P001", "Teclado", 250.556, 10);
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(producto, 2));
        venta.aplicarDescuento(10.0);

        Venta guardada = repositorio.guardar(venta);

        assertSame(venta, guardada);
        assertTrue(iniciada.get());
        assertTrue(confirmada.get());
        assertTrue(usoGetReference.get());
        assertTrue(cerrado.get());
    }

    @Test
    void siPersistFallaHaceRollbackYCierraElEntityManager() {
        AtomicBoolean iniciada = new AtomicBoolean(false);
        AtomicBoolean confirmada = new AtomicBoolean(false);
        AtomicBoolean revertida = new AtomicBoolean(false);
        AtomicBoolean cerrado = new AtomicBoolean(false);

        EntityManagerFactory emf = crearEmfSimulado(iniciada, confirmada, revertida, cerrado, new AtomicBoolean(), true);
        HibernateVentaRepository repositorio = new HibernateVentaRepository(emf);

        Producto producto = new Producto("P001", "Teclado", 100.0, 5);
        Venta venta = new Venta();
        venta.agregarPartida(new Partida(producto, 1));

        assertThrows(RuntimeException.class, () -> repositorio.guardar(venta));
        assertTrue(iniciada.get());
        assertFalse(confirmada.get());
        assertTrue(revertida.get());
        assertTrue(cerrado.get());
    }

    @Test
    void ventaMapperReconstruyeElModeloRespetandoInvariantes() {
        Producto producto = new Producto("P001", "Mouse", 120.456, 8);
        Venta original = new Venta();
        original.agregarPartida(new Partida(producto, 2));
        original.aplicarDescuento(15.0);

        VentaEntity entity = VentaMapper.toEntity(original, codigo -> ProductoMapper.toEntity(producto));
        assertEquals(new BigDecimal("15.00"), entity.getDescuento());
        assertEquals(new BigDecimal("225.91"), entity.getTotal());

        Venta reconstruida = VentaMapper.toModelVenta(entity);
        assertEquals(1, reconstruida.getPartidas().size());
        assertEquals(15.0, reconstruida.getDescuento());
        // Verifica que la lista interna de la venta reconstruida sigue siendo mutable a través de agregarPartida
        reconstruida.agregarPartida(new Partida(producto, 1));
        assertEquals(2, reconstruida.getPartidas().size());
    }

    @Test
    void laConfiguracionEsVaciaSiFaltaAlgunaVariable() {
        assertTrue(ConfiguracionHibernate.desde(Map.of()).isEmpty());
        assertTrue(ConfiguracionHibernate.desde(Map.of("DB_URL", "jdbc:postgresql://localhost/x")).isEmpty());
    }

    @Test
    void laConfiguracionNuncaMuestraElPassword() {
        ConfiguracionHibernate configuracion = ConfiguracionHibernate.desde(Map.of(
                "DB_URL", "jdbc:postgresql://localhost:5432/ventas",
                "DB_USER", "ventas_app",
                "DB_PASSWORD", "secreto-sintetico")).orElseThrow();

        assertFalse(configuracion.toString().contains("secreto-sintetico"));
        assertTrue(configuracion.toString().contains("***"));
    }

    private static EntityManagerFactory emfQueCuenta(AtomicInteger contador) {
        return (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("createEntityManager")) {
                        contador.incrementAndGet();
                    }
                    throw new UnsupportedOperationException("No se esperaba abrir el EntityManager");
                });
    }

    private static EntityManagerFactory crearEmfSimulado(AtomicBoolean begun,
                                                         AtomicBoolean committed,
                                                         AtomicBoolean rolledBack,
                                                         AtomicBoolean closed,
                                                         AtomicBoolean usedGetRef,
                                                         boolean fallarEnPersist) {
        EntityTransaction tx = (EntityTransaction) Proxy.newProxyInstance(
                EntityTransaction.class.getClassLoader(),
                new Class<?>[]{EntityTransaction.class},
                (proxy, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "begin":
                            begun.set(true);
                            return null;
                        case "commit":
                            committed.set(true);
                            return null;
                        case "rollback":
                            rolledBack.set(true);
                            return null;
                        case "isActive":
                            return begun.get() && !committed.get() && !rolledBack.get();
                        default:
                            throw new UnsupportedOperationException(metodo.getName());
                    }
                });

        EntityManager em = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class<?>[]{EntityManager.class},
                (proxy, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "getTransaction":
                            return tx;
                        case "getReference":
                            usedGetRef.set(true);
                            return new ProductoEntity((String) args[1], "Ref", BigDecimal.TEN, 1);
                        case "persist":
                            if (fallarEnPersist) {
                                throw new RuntimeException("Fallo simulado en persist");
                            }
                            if (args[0] instanceof VentaEntity) {
                                ((VentaEntity) args[0]).setId(1L);
                            }
                            return null;
                        case "close":
                            closed.set(true);
                            return null;
                        default:
                            throw new UnsupportedOperationException(metodo.getName());
                    }
                });

        return (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("createEntityManager")) {
                        return em;
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });
    }
}