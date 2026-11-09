package com.ventas.application.port.in;
import com.ventas.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ConsultarProductosUseCase {

    List<Producto> consultarTodos();

    default Optional<Producto> consultarPorId(String codigo) {
        return consultarTodos().stream()
                .filter(producto -> producto.getCodigo().equals(codigo))
                .findFirst();
    }
}
