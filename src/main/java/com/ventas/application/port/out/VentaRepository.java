package com.ventas.application.port.out;

import com.ventas.model.Venta;

public interface VentaRepository {

    Venta guardar(Venta venta);
}
