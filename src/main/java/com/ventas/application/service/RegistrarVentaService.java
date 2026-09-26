package com.ventas.application.service;

import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.PoliticaDescuento;
import com.ventas.model.Venta;

public class RegistrarVentaService implements RegistrarVentaUseCase {

    private final VentaRepository ventaRepository;
    private final PoliticaDescuento politicaDescuento;
    public RegistrarVentaService(VentaRepository ventaRepository, PoliticaDescuento politicaDescuento) {
        if (ventaRepository == null || politicaDescuento==null) {
            throw new IllegalArgumentException("El repositorio de ventas y las politicas de descuento no pueden ser nulas.");
        }
        this.ventaRepository = ventaRepository;
        this.politicaDescuento = politicaDescuento;
    }

    @Override
    public Venta registrar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        double descuento = politicaDescuento.calcularDescuento(venta);
        venta.aplicarDescuento(descuento);
        return ventaRepository.guardar(venta);
    }
}
