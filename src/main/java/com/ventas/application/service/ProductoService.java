package com.ventas.application.service;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarProductoUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;

import java.util.List;

public class ProductoService implements RegistrarProductoUseCase, ConsultarProductosUseCase {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        if (productoRepository == null) {
            throw new IllegalArgumentException("El repositorio de productos no puede ser nulo.");
        }
        this.productoRepository = productoRepository;
    }

    @Override
    public Producto registrar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        return productoRepository.guardar(producto);
    }

    @Override
    public List<Producto> consultarTodos() {
        return productoRepository.obtenerTodos();
    }
}

