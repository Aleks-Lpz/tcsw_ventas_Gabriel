package com.ventas.adapter.out.hibernate;

import com.ventas.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HibernateProductoRepositoryTest {

    @Test
    void testGuardar() {
        FakeEntityManagerFactory factory = new FakeEntityManagerFactory();
        HibernateProductoRepository repository = new HibernateProductoRepository(factory);
        Producto producto = new Producto("P001", "Producto de Prueba", 100.0, 10);
        
        Producto resultado = repository.guardar(producto);

        assertNotNull(resultado);
        FakeEntityManager em = factory.getUltimoEntityManager();
        assertNotNull(em);
        assertEquals(1, em.getPersistedEntities().size());
        
        ProductoEntity entityGuardada = (ProductoEntity) em.getPersistedEntities().get(0);
        assertEquals("P001", entityGuardada.getCodigo());
        
        FakeEntityTransaction tx = (FakeEntityTransaction) em.getTransaction();
        assertEquals(true, tx.isBegun());
        assertEquals(true, tx.isCommitted());
        assertEquals(true, em.isClosed());
    }

    @Test
    void testObtenerTodos() {
        FakeEntityManagerFactory factory = new FakeEntityManagerFactory();
        HibernateProductoRepository repository = new HibernateProductoRepository(factory);
        
        List<Producto> resultados = repository.obtenerTodos();
        
        assertNotNull(resultados);
        FakeEntityManager em = factory.getUltimoEntityManager();
        assertNotNull(em);
        
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
        }
        
        @Override
        public <T> T find(Class<T> entityClass, Object primaryKey) {
            if (!persistedEntities.isEmpty() && entityClass.isInstance(persistedEntities.get(0))) {
                return (T) persistedEntities.get(0);
            }
            return null;
        }

        @Override
        public jakarta.persistence.Query createQuery(String qlString) {
            return new FakeQuery();
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
    
    private static class FakeQuery implements jakarta.persistence.Query {
        @Override
        public java.util.List getResultList() {
            java.util.List<ProductoEntity> list = new java.util.ArrayList<>();
            list.add(new ProductoEntity("P001", "Fake", BigDecimal.valueOf(10.0), 5));
            return list;
        }

        // Métodos requeridos...
        @Override public Object getSingleResult() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public int executeUpdate() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setMaxResults(int maxResult) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public int getMaxResults() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setFirstResult(int startPosition) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public int getFirstResult() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setHint(String hintName, Object value) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public java.util.Map<String, Object> getHints() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.Query setParameter(jakarta.persistence.Parameter<T> param, T value) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(jakarta.persistence.Parameter<java.util.Calendar> param, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(jakarta.persistence.Parameter<java.util.Date> param, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(String name, Object value) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(String name, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(String name, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(int position, Object value) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(int position, java.util.Calendar value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setParameter(int position, java.util.Date value, jakarta.persistence.TemporalType temporalType) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public java.util.Set<jakarta.persistence.Parameter<?>> getParameters() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Parameter<?> getParameter(String name) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.Parameter<T> getParameter(String name, Class<T> type) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Parameter<?> getParameter(int position) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> jakarta.persistence.Parameter<T> getParameter(int position, Class<T> type) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public boolean isBound(jakarta.persistence.Parameter<?> param) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T getParameterValue(jakarta.persistence.Parameter<T> param) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public Object getParameterValue(String name) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public Object getParameterValue(int position) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setFlushMode(jakarta.persistence.FlushModeType flushMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.FlushModeType getFlushMode() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.Query setLockMode(jakarta.persistence.LockModeType lockMode) { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public jakarta.persistence.LockModeType getLockMode() { throw new UnsupportedOperationException("No soportado en Fake"); }
        @Override public <T> T unwrap(Class<T> cls) { throw new UnsupportedOperationException("No soportado en Fake"); }
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