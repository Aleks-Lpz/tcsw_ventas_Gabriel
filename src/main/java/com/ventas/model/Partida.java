package com.ventas.model;

public class Partida {
    private Producto producto;
    private int cantidad;

    public Partida(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }   
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad en la partida debe ser mayor a cero.");
        }
        this.cantidad = cantidad;
        this.producto = producto;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}
