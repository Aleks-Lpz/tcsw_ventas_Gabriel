package com.ventas.adapter.out.notification;

import com.ventas.model.EventoDeDominio;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaConfirmada;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReciboObservadorTest {

    @Test
    void alRecibirVentaConfirmadaEmiteRecibo() {
        ReciboObservador observador = new ReciboObservador();
        Venta venta = crearVentaConTotal(500.0);
        VentaConfirmada evento = new VentaConfirmada(venta);

        observador.alRecibir(evento);

        assertEquals(1, observador.getRecibosEmitidos().size());
        assertEquals("Recibo emitido para venta de $500.0", observador.getRecibosEmitidos().get(0));
    }

    @Test
    void ignoraEventosQueNoSonVentaConfirmada() {
        ReciboObservador observador = new ReciboObservador();
        EventoDeDominio otroEvento = new EventoDeDominio() {};

        observador.alRecibir(otroEvento);

        assertEquals(0, observador.getRecibosEmitidos().size());
    }

    @Test
    void emiteMultiplesRecibosParaMultiplesVentas() {
        ReciboObservador observador = new ReciboObservador();

        observador.alRecibir(new VentaConfirmada(crearVentaConTotal(500.0)));
        observador.alRecibir(new VentaConfirmada(crearVentaConTotal(1000.0)));

        assertEquals(2, observador.getRecibosEmitidos().size());
        assertEquals("Recibo emitido para venta de $500.0", observador.getRecibosEmitidos().get(0));
        assertEquals("Recibo emitido para venta de $1000.0", observador.getRecibosEmitidos().get(1));
    }

    @Test
    void losRecibosEmitidosNoSonModificables() {
        ReciboObservador observador = new ReciboObservador();
        observador.alRecibir(new VentaConfirmada(crearVentaConTotal(500.0)));

        assertThrows(UnsupportedOperationException.class,
                () -> observador.getRecibosEmitidos().add("recibo falso"));
    }

    private Venta crearVentaConTotal(double precioUnitario) {
        Producto producto = new Producto("P001", "Teclado", precioUnitario, 10);
        Partida partida = new Partida(producto, 1);
        Venta venta = new Venta();
        venta.agregarPartida(partida);
        return venta;
    }
}
