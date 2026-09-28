package com.ventas.adapter.out.memory;

import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryProductoRepository implements ProductoRepository {

    private final List<Producto> productos = new ArrayList<>();

    @Override
    public Producto guardar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        productos.add(producto);
        return producto;
    }

    public List<Producto> obtenerTodos() {
        return Collections.unmodifiableList(productos);
    }
}

