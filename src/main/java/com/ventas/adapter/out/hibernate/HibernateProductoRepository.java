package com.ventas.adapter.out.hibernate;

import java.util.List;

import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class HibernateProductoRepository implements ProductoRepository {
    EntityManagerFactory entityManagerFactory;
    

    @Override
        public Producto guardar(Producto producto) {
        ProductoEntity productoEntity = ProductoMapper.toEntity(producto);
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(productoEntity);
        em.getTransaction().commit();
        productoEntity = em.find(ProductoEntity.class, productoEntity.getCodigo());
        em.close();
        return ProductoMapper.toModel(productoEntity);
    }

    @Override 
    public List<Producto> obtenerTodos() {
        EntityManager em = entityManagerFactory.createEntityManager();

    }

    

}
