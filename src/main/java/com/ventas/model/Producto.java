package com.ventas.model;

public class Producto {
    private String codigo;
    private String nombre;
    private double precio;
    private int existencia;

    public Producto(String codigo, String nombre, double precio, int existencia) {
        setCodigo(codigo);
        setNombre(nombre);
        setPrecio(precio);
        setExistencia(existencia);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código no puede estar vacío.");
        }
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor a cero");
        }
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
        this.existencia = existencia;
    }
}
