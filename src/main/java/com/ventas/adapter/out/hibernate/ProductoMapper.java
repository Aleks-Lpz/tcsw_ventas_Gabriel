package com.ventas.adapter.out.hibernate;

import java.math.BigDecimal;

import com.ventas.model.Producto;

public class ProductoMapper {
    
    private ProductoMapper() {

    }
    public static ProductoEntity toEntity(Producto producto) {
        BigDecimal bd = BigDecimal.valueOf(producto.getPrecio());
        return new ProductoEntity(producto.getCodigo(), producto.getNombre(), 
        bd, producto.getExistencia());
    }

    public static Producto toModel(ProductoEntity productoEntity) {
        return new Producto(productoEntity.getCodigo(), productoEntity.getNombre(), 
        productoEntity.getPrecio().doubleValue(), productoEntity.getExistencia());
    }
}
