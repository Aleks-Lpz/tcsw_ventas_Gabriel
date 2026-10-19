package com.ventas.application.port.out;

import com.ventas.model.Producto;

import java.util.Collections;
import java.util.List;

public interface ProductoRepository {

    Producto guardar(Producto producto);

    default List<Producto> obtenerTodos() {
        return Collections.emptyList();
    }
}

