package com.ventas.application.service;

import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.PublicadorDeEventos;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Venta;
import com.ventas.model.VentaConfirmada;

public class RegistrarVentaService implements RegistrarVentaUseCase {

    private final VentaRepository ventaRepository;
    private final PublicadorDeEventos publicadorDeEventos;

    public RegistrarVentaService(VentaRepository ventaRepository, PublicadorDeEventos publicadorDeEventos) {
        if (ventaRepository == null) {
            throw new IllegalArgumentException("El repositorio de ventas no puede ser nulo.");
        }
        if (publicadorDeEventos == null) {
            throw new IllegalArgumentException("El publicador de eventos no puede ser nulo.");
        }
        this.ventaRepository = ventaRepository;
        this.publicadorDeEventos = publicadorDeEventos;
    }

    @Override
    public Venta registrar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        Venta ventaGuardada = ventaRepository.guardar(venta);
        publicadorDeEventos.publicar(new VentaConfirmada(ventaGuardada));
        return ventaGuardada;
    }
}
