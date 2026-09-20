package com.ventas.application.service;

import com.ventas.application.port.out.VentaRepository;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarVentaServiceTest {

    @Test
    void unaVentaValidaPuedeRegistrarse() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repository = new FakeVentaRepository();
        RegistrarVentaService service = new RegistrarVentaService(repository);

        Venta resultado = service.registrar(venta);

        assertNotNull(resultado);
        assertEquals(venta, resultado);
        assertEquals(1, repository.getCantidadGuardados());
    }

    @Test
    void elServicioUsaElVentaRepositoryParaGuardarLaVenta() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repository = new FakeVentaRepository();
        RegistrarVentaService service = new RegistrarVentaService(repository);

        service.registrar(venta);

        assertEquals(1, repository.getCantidadGuardados());
        assertSame(venta, repository.getUltimaVentaGuardada());
    }

    @Test
    void laVentaDevueltaCorrespondeALaVentaRegistrada() {
        Venta venta = crearVentaValida();
        FakeVentaRepository repository = new FakeVentaRepository();
        RegistrarVentaService service = new RegistrarVentaService(repository);

        Venta resultado = service.registrar(venta);

        assertSame(venta, resultado);
        assertSame(venta, repository.getUltimaVentaGuardada());
    }

    @Test
    void unaVentaNullEsRechazada() {
        FakeVentaRepository repository = new FakeVentaRepository();
        RegistrarVentaService service = new RegistrarVentaService(repository);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(null));

        assertEquals("La venta no puede ser nula.", exception.getMessage());
    }

    @Test
    void noSePuedeCrearElServicioSinVentaRepository() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new RegistrarVentaService(null));

        assertEquals("El repositorio de ventas no puede ser nulo.", exception.getMessage());
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
}
