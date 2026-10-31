package com.ventas.adapter.out.hibernate;

import com.ventas.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HibernateProductoRepositoryTest {

    @Test
    void noSePuedeCrearSinEntityManagerFactory() {
        assertThrows(NullPointerException.class, () -> new HibernateProductoRepository(null));
    }

    @Test
    void guardarProductoNuloLanzaIllegalArgumentException() {
        EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                (p, m, a) -> {
                    throw new UnsupportedOperationException("No debe abrir EntityManager");
                });
        HibernateProductoRepository repo = new HibernateProductoRepository(emf);
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(null));
    }

    @Test
    void guardarRedondeaPrecioPersisteYCierraEntityManager() {
        AtomicBoolean iniciada = new AtomicBoolean(false);
        AtomicBoolean confirmada = new AtomicBoolean(false);
        AtomicBoolean cerrado = new AtomicBoolean(false);
        AtomicReference<ProductoEntity> entidadPersistida = new AtomicReference<>();

        EntityManagerFactory emf = crearEmfParaGuardar(iniciada, confirmada, new AtomicBoolean(), cerrado, entidadPersistida, false);
        HibernateProductoRepository repo = new HibernateProductoRepository(emf);

        Producto producto = new Producto("P001", "Monitor", 3500.556, 15);
        Producto resultado = repo.guardar(producto);

        assertNotNull(resultado);
        assertEquals(3500.56, resultado.getPrecio());
        assertEquals(new BigDecimal("3500.56"), entidadPersistida.get().getPrecio());
        assertTrue(iniciada.get());
        assertTrue(confirmada.get());
        assertTrue(cerrado.get());
    }

    @Test
    void siGuardarFallaHaceRollbackYCierraEntityManager() {
        AtomicBoolean iniciada = new AtomicBoolean(false);
        AtomicBoolean confirmada = new AtomicBoolean(false);
        AtomicBoolean revertida = new AtomicBoolean(false);
        AtomicBoolean cerrado = new AtomicBoolean(false);

        EntityManagerFactory emf = crearEmfParaGuardar(
                iniciada, confirmada, revertida, cerrado, new AtomicReference<>(), true);
        HibernateProductoRepository repo = new HibernateProductoRepository(emf);

        Producto producto = new Producto("P001", "Monitor", 100.0, 5);
        assertThrows(RuntimeException.class, () -> repo.guardar(producto));

        assertTrue(iniciada.get());
        assertFalse(confirmada.get());
        assertTrue(revertida.get());
        assertTrue(cerrado.get());
    }

    @Test
    void obtenerTodosUsaTypedQuerySinTransaccionYCierraEntityManager() {
        AtomicBoolean cerrado = new AtomicBoolean(false);
        AtomicBoolean pidioTransaccion = new AtomicBoolean(false);

        @SuppressWarnings("unchecked")
        TypedQuery<ProductoEntity> typedQuery = (TypedQuery<ProductoEntity>) Proxy.newProxyInstance(
                TypedQuery.class.getClassLoader(),
                new Class<?>[]{TypedQuery.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("getResultList")) {
                        return List.of(new ProductoEntity("P001", "Teclado", new BigDecimal("250.00"), 10));
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });

        EntityManager em = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class<?>[]{EntityManager.class},
                (proxy, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "createQuery":
                            assertEquals(2, args.length, "Debe usar createQuery tipado con ProductoEntity.class");
                            assertEquals(ProductoEntity.class, args[1]);
                            return typedQuery;
                        case "getTransaction":
                            pidioTransaccion.set(true);
                            throw new UnsupportedOperationException("Lectura no necesita transaccion");
                        case "close":
                            cerrado.set(true);
                            return null;
                        default:
                            throw new UnsupportedOperationException(metodo.getName());
                    }
                });

        EntityManagerFactory emf = (EntityManagerFactory) Proxy.newProxyInstance(
                EntityManagerFactory.class.getClassLoader(),
                new Class<?>[]{EntityManagerFactory.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("createEntityManager")) {
                        return em;
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });

        HibernateProductoRepository repo = new HibernateProductoRepository(emf);
        List<Producto> productos = repo.obtenerTodos();

        assertEquals(1, productos.size());
        assertEquals("P001", productos.get(0).getCodigo());
        assertFalse(pidioTransaccion.get());
        assertTrue(cerrado.get());
    }

    private static EntityManagerFactory crearEmfParaGuardar(AtomicBoolean begun,
                                                            AtomicBoolean committed,
                                                            AtomicBoolean rolledBack,
                                                            AtomicBoolean closed,
                                                            AtomicReference<ProductoEntity> guardada,
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
                        case "persist":
                            if (fallarEnPersist) {
                                throw new RuntimeException("Fallo simulado en persist");
                            }
                            guardada.set((ProductoEntity) args[0]);
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