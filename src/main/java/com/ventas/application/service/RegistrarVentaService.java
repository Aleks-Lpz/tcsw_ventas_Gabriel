package com.ventas.application.service;

import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Venta;

public class RegistrarVentaService implements RegistrarVentaUseCase {

    private final VentaRepository ventaRepository;

    public RegistrarVentaService(VentaRepository ventaRepository) {
        if (ventaRepository == null) {
            throw new IllegalArgumentException("El repositorio de ventas no puede ser nulo.");
        }
        this.ventaRepository = ventaRepository;
    }

    @Override
    public Venta registrar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        return ventaRepository.guardar(venta);
    }
}
