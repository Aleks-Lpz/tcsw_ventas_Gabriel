package com.ventas.adapter.out.memory;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Venta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryVentaRepository implements VentaRepository {

    private final List<Venta> ventas = new ArrayList<>();

    @Override
    public Venta guardar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        ventas.add(venta);
        return venta;
    }

    public List<Venta> obtenerTodas() {
        return Collections.unmodifiableList(ventas);
    }
}
