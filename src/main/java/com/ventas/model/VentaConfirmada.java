package com.ventas.model;

public class VentaConfirmada implements EventoDeDominio {

    private final Venta venta;

    public VentaConfirmada(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        this.venta = venta;
    }

    public Venta getVenta() {
        return venta;
    }
}
