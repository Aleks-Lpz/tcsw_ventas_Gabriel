package com.ventas.application.port.out;

import com.ventas.model.Producto;

public interface ProductoRepository {

    Producto guardar(Producto producto);
}

