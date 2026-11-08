package com.ventas.adapter.in.rest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class ProductoRequest {
    @NotBlank
    private String codigo;
    @NotBlank
    private String nombre;
    @NotNull
    @Positive
    private Double precio;
    @NotNull
    @PositiveOrZero
    private Integer existencia;

    public String getCodigo() {
         return codigo; 
    }
    public void setCodigo(String codigo) { 
        this.codigo = codigo; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    public Double getPrecio() { 
        return precio; 
    }
    public void setPrecio(Double precio) { 
        this.precio = precio; 
    }
    public Integer getExistencia() { 
        return existencia; 
    }
    public void setExistencia(Integer existencia) { 
        this.existencia = existencia;
    }
}
