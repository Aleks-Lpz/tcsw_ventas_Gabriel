package com.ventas.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venta {

    private Long id;
    private final ZonedDateTime fecha;
    private List<Partida> partidas;
    private double descuento;

    public Venta() {
        this.partidas = new ArrayList<>();
        this.fecha = ZonedDateTime.now();
    }

    /** Identificador asignado al persistir la venta; null mientras no se ha guardado. */
    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id de la venta debe ser positivo.");
        }
        this.id = id;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void agregarPartida(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula.");
        }
        this.partidas.add(partida);
    }

    public List<Partida> getPartidas() {
        return Collections.unmodifiableList(partidas);
    }

    public double calcularTotal() {
        double total = 0.0;
        for (Partida partida : partidas) {
            total += partida.getSubtotal();
        }
        total = total - descuento;
        return total;
    }

    public void aplicarDescuento(Double descuento) {
        if (descuento >= 0) {
            this.descuento = descuento;
        } else {
            throw new IllegalArgumentException("Un descuento no puede ser menor a cero");
        }
    }

    public double getDescuento() {
        return this.descuento;
    }
}
