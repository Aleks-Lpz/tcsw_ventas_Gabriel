package com.ventas.adapter.out.hibernate;

import com.ventas.model.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoEntity toEntity(Producto producto) {
        BigDecimal precio = BigDecimal.valueOf(producto.getPrecio()).setScale(2, RoundingMode.HALF_UP);
        return new ProductoEntity(
                producto.getCodigo(),
                producto.getNombre(),
                precio,
                producto.getExistencia()
        );
    }

    public static Producto toModel(ProductoEntity productoEntity) {
        return new Producto(
                productoEntity.getCodigo(),
                productoEntity.getNombre(),
                productoEntity.getPrecio().doubleValue(),
                productoEntity.getExistencia()
        );
    }
}