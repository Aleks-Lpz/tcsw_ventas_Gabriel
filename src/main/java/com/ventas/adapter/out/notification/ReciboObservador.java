package com.ventas.adapter.out.notification;

import com.ventas.application.port.in.ObservadorDeEventos;
import com.ventas.model.EventoDeDominio;
import com.ventas.model.VentaConfirmada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReciboObservador implements ObservadorDeEventos {

    private final List<String> recibosEmitidos = new ArrayList<>();

    @Override
    public void alRecibir(EventoDeDominio evento) {
        if (evento instanceof VentaConfirmada) {
            VentaConfirmada ventaConfirmada = (VentaConfirmada) evento;
            double total = ventaConfirmada.getVenta().calcularTotal();
            recibosEmitidos.add("Recibo emitido para venta de $" + total);
        }
    }

    public List<String> getRecibosEmitidos() {
        return Collections.unmodifiableList(recibosEmitidos);
    }
}
