package com.ventas.adapter.out.hibernate;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Venta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class HibernateVentaRepository implements VentaRepository {
    private EntityManagerFactory entityManagerFactory;

    public HibernateVentaRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory=entityManagerFactory;
    }
    @Override
    public Venta guardar(Venta venta) {
    EntityManager em = entityManagerFactory.createEntityManager();
    em.getTransaction().begin();
    VentaEntity ventaEntity = VentaMapper.toEntity(venta);
    em.persist(ventaEntity);
    em.getTransaction().commit();
    em.close();

    return VentaMapper.toModelVenta(ventaEntity);
    }

}
