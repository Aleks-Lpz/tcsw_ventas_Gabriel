package com.ventas.application.service;

import com.ventas.application.port.out.PublicadorDeEventos;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.EventoDeDominio;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaConfirmada;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarVentaServiceTest {

    @Test
    void unaVentaValidaPuedeRegistrarse() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        Venta resultado = servicio.registrar(venta);

        assertNotNull(resultado);
        assertEquals(venta, resultado);
        assertEquals(1, repositorio.getCantidadGuardados());
    }

    @Test
    void elServicioUsaElVentaRepositoryParaGuardarLaVenta() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        servicio.registrar(venta);

        assertEquals(1, repositorio.getCantidadGuardados());
        assertSame(venta, repositorio.getUltimaVentaGuardada());
    }

    @Test
    void laVentaDevueltaCorrespondeALaVentaRegistrada() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        Venta resultado = servicio.registrar(venta);

        assertSame(venta, resultado);
        assertSame(venta, repositorio.getUltimaVentaGuardada());
    }

    @Test
    void unaVentaNullEsRechazada() {
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(null));

        assertEquals("La venta no puede ser nula.", excepcion.getMessage());
    }

    @Test
    void noSePuedeCrearElServicioSinVentaRepository() {
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarVentaService(null, publicador));

        assertEquals("El repositorio de ventas no puede ser nulo.", excepcion.getMessage());
    }

    @Test
    void noSePuedeCrearElServicioSinPublicador() {
        FakeVentaRepository repositorio = new FakeVentaRepository();

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarVentaService(repositorio, null));

        assertEquals("El publicador de eventos no puede ser nulo.", excepcion.getMessage());
    }

    @Test
    void alRegistrarUnaVentaSePublicaVentaConfirmada() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        servicio.registrar(venta);

        assertEquals(1, publicador.getEventosPublicados().size());
        assertTrue(publicador.getEventosPublicados().get(0) instanceof VentaConfirmada);
    }

    @Test
    void elEventoPublicadoContienelaVentaRegistrada() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repositorio = new FakeVentaRepository();
        FakePublicadorDeEventos publicador = new FakePublicadorDeEventos();
        RegistrarVentaService servicio = new RegistrarVentaService(repositorio, publicador);

        servicio.registrar(venta);

        VentaConfirmada evento = (VentaConfirmada) publicador.getEventosPublicados().get(0);
        assertSame(venta, evento.getVenta());
    }

    private Venta crearVentaValida() {
        Producto producto = new Producto("P001", "Teclado", 250.0, 10);
        Partida partida = new Partida(producto, 2);

        Venta venta = new Venta();
        venta.agregarPartida(partida);
        return venta;
    }

    private static class FakeVentaRepository implements VentaRepository {
        private int cantidadGuardados = 0;
        private Venta ultimaVentaGuardada;

        @Override
        public Venta guardar(Venta venta) {
            this.cantidadGuardados++;
            this.ultimaVentaGuardada = venta;
            return venta;
        }

        public int getCantidadGuardados() {
            return cantidadGuardados;
        }

        public Venta getUltimaVentaGuardada() {
            return ultimaVentaGuardada;
        }
    }

    private static class FakePublicadorDeEventos implements PublicadorDeEventos {
        private final List<EventoDeDominio> eventosPublicados = new ArrayList<>();

        @Override
        public void publicar(EventoDeDominio evento) {
            eventosPublicados.add(evento);
        }

        public List<EventoDeDominio> getEventosPublicados() {
            return eventosPublicados;
        }
    }
}
