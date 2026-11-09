package com.ventas.adapter.in.rest;

public class ProductoResponse {
    private final String codigo;
    private final String nombre;
    private final double precio;
    private final int existencia;

    public ProductoResponse(String codigo, String nombre, double precio, int existencia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.existencia = existencia;
    }

    public String getCodigo() { 
        return codigo; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public double getPrecio() { 
        return precio; 
    }
    public int getExistencia() { 
        return existencia;
    }
}
