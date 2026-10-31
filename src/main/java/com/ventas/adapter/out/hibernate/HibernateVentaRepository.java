package com.ventas.adapter.out.hibernate;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Partida;
import com.ventas.model.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.Objects;

public class HibernateVentaRepository implements VentaRepository {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateVentaRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(
                entityManagerFactory, "El EntityManagerFactory no puede ser nulo.");
    }

    @Override
    public Venta guardar(Venta venta) {
        insertar(venta);
        return venta;
    }

    /**
     * Inserta la venta con sus partidas en una sola transaccion y devuelve el id generado.
     */
    public long insertar(Venta venta) {
        validar(venta);
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            VentaEntity ventaEntity = VentaMapper.toEntity(
                    venta,
                    codigo -> em.getReference(ProductoEntity.class, codigo)
            );
            em.persist(ventaEntity);
            tx.commit();
            return ventaEntity.getId();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void validar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        if (venta.getPartidas().isEmpty()) {
            throw new IllegalArgumentException("Una venta sin partidas no puede persistirse.");
        }
        for (Partida partida : venta.getPartidas()) {
            if (partida.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad en la partida debe ser mayor a cero.");
            }
        }
    }
}
