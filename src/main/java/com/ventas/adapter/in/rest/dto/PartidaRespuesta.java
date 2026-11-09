package com.ventas.adapter.in.rest.dto;

/**
 * Detalle de una partida en {@link VentaRespuesta} (esquema PartidaRespuesta de docs/openapi.yaml).
 */
public class PartidaRespuesta {

    private final Long productoId;
    private final String nombre;
    private final int cantidad;
    private final double precioUnitario;
    private final double subtotal;

    public PartidaRespuesta(Long productoId, String nombre, int cantidad, double precioUnitario, double subtotal) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
