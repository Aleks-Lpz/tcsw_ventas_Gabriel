package com.ventas.adapter.out.hibernate;

import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HibernateProductoRepository implements ProductoRepository {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateProductoRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(
                entityManagerFactory, "El EntityManagerFactory no puede ser nulo.");
    }

    @Override
    public Producto guardar(Producto producto) {
        validar(producto);
        ProductoEntity productoEntity = ProductoMapper.toEntity(producto);
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(productoEntity);
            tx.commit();
            return ProductoMapper.toModel(productoEntity);
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> obtenerTodos() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<ProductoEntity> listEntities = em
                    .createQuery("SELECT p FROM ProductoEntity p", ProductoEntity.class)
                    .getResultList();
            List<Producto> productos = new ArrayList<>();
            for (ProductoEntity entity : listEntities) {
                productos.add(ProductoMapper.toModel(entity));
            }
            return productos;
        } finally {
            em.close();
        }
    }

    private void validar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (producto.getExistencia() < 0 || producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("Las existencias y el precio no pueden ser negativos");
        }
    }
}