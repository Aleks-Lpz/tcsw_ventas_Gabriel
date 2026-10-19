package com.ventas.adapter.in.swing.DTO;

public class PartidaDTO {
    private final String codigo;
    private final String nombre;
    private final double precioUnitario;
    private final int cantidad;
    private final double subtotal;

    public PartidaDTO(String codigo, String nombre, double precioUnitario, int cantidad, double subtotal) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
