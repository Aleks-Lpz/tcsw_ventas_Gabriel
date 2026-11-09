package com.ventas.adapter.in.rest;

import com.ventas.model.Producto;

public final class ProductoRestMapper {
    private ProductoRestMapper() { }

    public static Producto toDomain(ProductoRequest request) {
        return new Producto(request.getCodigo(), request.getNombre(),
                request.getPrecio(), request.getExistencia());
    }

    public static ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(producto.getCodigo(), producto.getNombre(),
                producto.getPrecio(), producto.getExistencia());
    }
}
