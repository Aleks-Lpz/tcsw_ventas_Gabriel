package com.ventas.application.service;

import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.PublicadorDeEventos;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Partida;
import com.ventas.model.PoliticaDescuento;
import com.ventas.model.Venta;
import com.ventas.model.VentaConfirmada;
import com.ventas.model.VentaFactory;

import java.util.List;

public class RegistrarVentaService implements RegistrarVentaUseCase {

    private final VentaRepository ventaRepository;
    private final PoliticaDescuento politicaDescuento;
    private final PublicadorDeEventos publicadorDeEventos;

    public RegistrarVentaService(VentaRepository ventaRepository, PoliticaDescuento politicaDescuento, PublicadorDeEventos publicadorDeEventos) {
        if (ventaRepository == null || politicaDescuento == null || publicadorDeEventos == null) {
            throw new IllegalArgumentException("Las dependencias (repositorio, política, publicador) no pueden ser nulas.");
        }
        this.ventaRepository = ventaRepository;
        this.politicaDescuento = politicaDescuento;
        this.publicadorDeEventos = publicadorDeEventos;
    }

    @Override
    public Venta registrar(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula.");
        }
        
        double descuento = politicaDescuento.calcularDescuento(venta);
        venta.aplicarDescuento(descuento);
        
        Venta ventaGuardada = ventaRepository.guardar(venta);
        
        publicadorDeEventos.publicar(new VentaConfirmada(ventaGuardada));
        
        return ventaGuardada;
    }

    public Venta registrarDesdePartidas(List<Partida> partidas) {
        Venta nuevaVenta = VentaFactory.crearVenta(partidas);
        return this.registrar(nuevaVenta); 
    }
}