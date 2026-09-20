package com.ventas.adapter.out.memory;

import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryVentaRepositoryTest {

    @Test
    void guardarUnaVenta() {
        InMemoryVentaRepository repository = new InMemoryVentaRepository();
        Venta venta = crearVentaValida();

        Venta guardada = repository.guardar(venta);

        assertSame(venta, guardada);
        assertEquals(1, repository.obtenerTodas().size());
    }

    @Test
    void recuperarLaVentaGuardada() {
        InMemoryVentaRepository repository = new InMemoryVentaRepository();
        Venta venta = crearVentaValida();

        repository.guardar(venta);

        assertSame(venta, repository.obtenerTodas().get(0));
    }

    @Test
    void guardarVariasVentas() {
        InMemoryVentaRepository repository = new InMemoryVentaRepository();
        Venta venta1 = crearVentaValida();
        Venta venta2 = crearVentaValida();

        repository.guardar(venta1);
        repository.guardar(venta2);

        List<Venta> ventas = repository.obtenerTodas();
        assertEquals(2, ventas.size());
        assertSame(venta1, ventas.get(0));
        assertSame(venta2, ventas.get(1));
    }

    @Test
    void obtenerTodasNoPermiteModificarLaColeccionInterna() {
        InMemoryVentaRepository repository = new InMemoryVentaRepository();
        Venta venta = crearVentaValida();
        repository.guardar(venta);

        List<Venta> ventas = repository.obtenerTodas();

        assertThrows(UnsupportedOperationException.class, () -> ventas.add(new Venta()));
    }

    @Test
    void unaVentaNullEsRechazada() {
        InMemoryVentaRepository repository = new InMemoryVentaRepository();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.guardar(null));

        assertEquals("La venta no puede ser nula.", exception.getMessage());
    }

    private Venta crearVentaValida() {
        Producto producto = new Producto("P002", "Mouse", 120.0, 8);
        Partida partida = new Partida(producto, 1);

        Venta venta = new Venta();
        venta.agregarPartida(partida);
        return venta;
    }
}
