package com.ventas.application.port.in;

import com.ventas.model.Producto;

import java.util.List;

public interface ConsultarProductosUseCase {

    List<Producto> consultarTodos();
}
