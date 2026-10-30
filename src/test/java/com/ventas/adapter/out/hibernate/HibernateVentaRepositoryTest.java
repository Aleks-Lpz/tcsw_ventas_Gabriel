package com.ventas.adapter.out.hibernate;

import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HibernateVentaRepositoryTest {

    @Test
    void testGuardar() {
        FakeEntityManagerFactory factory = new FakeEntityManagerFactory();
        HibernateVentaRepository repository = new HibernateVentaRepository(factory);
        
        Producto producto = new Producto("P001", "Test", 10.0, 5);
        Partida partida = new Partida(producto, 1);
        Venta venta = new Venta(Collections.singletonList(partida), 0.0);
        
        Venta resultado = repository.guardar(venta);

        assertNotNull(resultado);
        FakeEntityManager em = factory.getUltimoEntityManager();
        assertNotNull(em);
        assertEquals(1, em.getPersistedEntities().size());
        
        FakeEntityTransaction tx = (FakeEntityTransaction) em.getTransaction();
        assertEquals(true, tx.isBegun());
        assertEquals(true, tx.isCommitted());
        assertEquals(true, em.isClosed());
    }
    
    // Clases Fake para simular el comportamiento de JPA sin Mockito
    private static class FakeEntityManagerFactory implements EntityManagerFactory {
        private FakeEntityManager ultimoEntityManager;
        
        @Override
        public EntityManager createEntityManager() {
            ultimoEntityManager = new FakeEntityManager();
            return ultimoEntityManager;
        }
        
        public FakeEntityManager getUltimoEntityManager() {
            return ultimoEntityManager;
        }

        // Métodos no implementados requeridos por la interfaz...
        @Override public void close() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean isOpen() { return true; }
        @Override public EntityManager createEntityManager(java.util.Map map) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.criteria.CriteriaBuilder getCriteriaBuilder() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.metamodel.Metamodel getMetamodel() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public java.util.Map<String, Object> getProperties() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Cache getCache() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.PersistenceUnitUtil getPersistenceUnitUtil() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void addNamedQuery(String name, jakarta.persistence.Query query) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T unwrap(Class<T> cls) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> void addNamedEntityGraph(String graphName, jakarta.persistence.EntityGraph<T> entityGraph) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public EntityManager createEntityManager(jakarta.persistence.SynchronizationType synchronizationType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public EntityManager createEntityManager(jakarta.persistence.SynchronizationType synchronizationType, java.util.Map map) { throw new UnsupportedOperationException("No soportado en Fake"); }
    }
    
    private static class FakeEntityManager implements EntityManager {
        private final FakeEntityTransaction tx = new FakeEntityTransaction();
        private final java.util.List<Object> persistedEntities = new java.util.ArrayList<>();
        private boolean closed = false;
        
        @Override
        public EntityTransaction getTransaction() {
            return tx;
        }
        
        @Override
        public void persist(Object entity) {
            persistedEntities.add(entity);
            // Simulamos que la BD le asignó un ID
            if (entity instanceof VentaEntity) {
                ((VentaEntity) entity).setId(1L);
            }
        }
        
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey) {
            if (!persistedEntities.isEmpty() && entityClass.isInstance(persistedEntities.get(0))) {
                return (T) persistedEntities.get(0);
            }
            return null;
        }
        
        @Override
        public void close() {
            closed = true;
        }
        
        public java.util.List<Object> getPersistedEntities() {
            return persistedEntities;
        }
        
        public boolean isClosed() {
            return closed;
        }

        // Métodos no implementados requeridos por la interfaz...
        @Override public <T> T merge(T entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void remove(Object entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T find(Class<T> entityClass, Object primaryKey, java.util.Map<String, Object> properties) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T find(Class<T> entityClass, Object primaryKey, jakarta.persistence.LockModeType lockMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T find(Class<T> entityClass, Object primaryKey, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T getReference(Class<T> entityClass, Object primaryKey) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void flush() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void setFlushMode(jakarta.persistence.FlushModeType flushMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.FlushModeType getFlushMode() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void lock(Object entity, jakarta.persistence.LockModeType lockMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void lock(Object entity, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void refresh(Object entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void refresh(Object entity, java.util.Map<String, Object> properties) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void refresh(Object entity, jakarta.persistence.LockModeType lockMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void refresh(Object entity, jakarta.persistence.LockModeType lockMode, java.util.Map<String, Object> properties) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void clear() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void detach(Object entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean contains(Object entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.LockModeType getLockMode(Object entity) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void setProperty(String propertyName, Object value) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public java.util.Map<String, Object> getProperties() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createQuery(String qlString) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.TypedQuery<T> createQuery(jakarta.persistence.criteria.CriteriaQuery<T> criteriaQuery) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createQuery(jakarta.persistence.criteria.CriteriaUpdate updateQuery) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createQuery(jakarta.persistence.criteria.CriteriaDelete deleteQuery) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.TypedQuery<T> createQuery(String qlString, Class<T> resultClass) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createNamedQuery(String name) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createNativeQuery(String sqlString) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createNativeQuery(String sqlString, Class resultClass) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query createNativeQuery(String sqlString, String resultSetMapping) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.StoredProcedureQuery createNamedStoredProcedureQuery(String name) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void joinTransaction() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean isJoinedToTransaction() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T unwrap(Class<T> cls) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public Object getDelegate() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean isOpen() { return true; }
        @Override public jakarta.persistence.EntityManagerFactory getEntityManagerFactory() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.criteria.CriteriaBuilder getCriteriaBuilder() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.metamodel.Metamodel getMetamodel() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.EntityGraph<T> createEntityGraph(Class<T> rootType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.EntityGraph<?> createEntityGraph(String graphName) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.EntityGraph<?> getEntityGraph(String graphName) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> java.util.List<jakarta.persistence.EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) { throw new UnsupportedOperationException("No soportado en Fake"); }
    }
    
    private static class FakeEntityTransaction implements EntityTransaction {
        private boolean begun = false;
        private boolean committed = false;

        @Override public void begin() { begun = true; }
        @Override public void commit() { committed = true; }
        public boolean isBegun() { return begun; }
        public boolean isCommitted() { return committed; }
        @Override public void rollback() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public void setRollbackOnly() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean getRollbackOnly() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean isActive() { return begun && !committed; }
    }
}