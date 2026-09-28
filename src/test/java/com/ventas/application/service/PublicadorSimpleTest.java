package com.ventas.application.service;

import com.ventas.application.port.in.ObservadorDeEventos;
import com.ventas.model.EventoDeDominio;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaConfirmada;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicadorSimpleTest {

    @Test
    void publicarNotificaAlObservadorRegistrado() {
        PublicadorSimple publicador = new PublicadorSimple();
        ObservadorFalso observador = new ObservadorFalso();
        publicador.agregarObservador(observador);

        VentaConfirmada evento = new VentaConfirmada(crearVentaValida());
        publicador.publicar(evento);

        assertEquals(1, observador.getEventosRecibidos().size());
        assertSame(evento, observador.getEventosRecibidos().get(0));
    }

    @Test
    void publicarNotificaAMultiplesObservadores() {
        PublicadorSimple publicador = new PublicadorSimple();
        ObservadorFalso observador1 = new ObservadorFalso();
        ObservadorFalso observador2 = new ObservadorFalso();
        publicador.agregarObservador(observador1);
        publicador.agregarObservador(observador2);

        VentaConfirmada evento = new VentaConfirmada(crearVentaValida());
        publicador.publicar(evento);

        assertEquals(1, observador1.getEventosRecibidos().size());
        assertEquals(1, observador2.getEventosRecibidos().size());
    }

    @Test
    void sinObservadoresNoFalla() {
        PublicadorSimple publicador = new PublicadorSimple();
        VentaConfirmada evento = new VentaConfirmada(crearVentaValida());

        assertDoesNotThrow(() -> publicador.publicar(evento));
    }

    @Test
    void unEventoNuloEsRechazado() {
        PublicadorSimple publicador = new PublicadorSimple();

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> publicador.publicar(null));

        assertEquals("El evento no puede ser nulo.", excepcion.getMessage());
    }

    @Test
    void unObservadorNuloEsRechazado() {
        PublicadorSimple publicador = new PublicadorSimple();

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> publicador.agregarObservador(null));

        assertEquals("El observador no puede ser nulo.", excepcion.getMessage());
    }

    private Venta crearVentaValida() {
        Producto producto = new Producto("P001", "Teclado", 250.0, 10);
        Partida partida = new Partida(producto, 2);
        Venta venta = new Venta();
        venta.agregarPartida(partida);
        return venta;
    }

    private static class ObservadorFalso implements ObservadorDeEventos {
        private final List<EventoDeDominio> eventosRecibidos = new ArrayList<>();

        @Override
        public void alRecibir(EventoDeDominio evento) {
            eventosRecibidos.add(evento);
        }

        public List<EventoDeDominio> getEventosRecibidos() {
            return eventosRecibidos;
        }
    }
}
