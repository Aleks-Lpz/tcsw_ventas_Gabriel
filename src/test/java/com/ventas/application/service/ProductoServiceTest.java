package com.ventas.application.service;

import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    @Test
    void unProductoValidoPuedeRegistrarse() {
        Producto producto = crearProductoValido();
        FakeProductoRepository repositorio = new FakeProductoRepository();
        ProductoService servicio = new ProductoService(repositorio);

        Producto resultado = servicio.registrar(producto);

        assertNotNull(resultado);
        assertEquals(producto, resultado);
        assertEquals(1, repositorio.getCantidadGuardados());
    }

    @Test
    void elServicioUsaElRepositorioParaGuardarElProducto() {
        Producto producto = crearProductoValido();
        FakeProductoRepository repositorio = new FakeProductoRepository();
        ProductoService servicio = new ProductoService(repositorio);

        servicio.registrar(producto);

        assertEquals(1, repositorio.getCantidadGuardados());
        assertSame(producto, repositorio.getUltimoProductoGuardado());
    }

    @Test
    void elProductoDevueltoCorrespondeAlProductoRegistrado() {
        Producto producto = crearProductoValido();
        FakeProductoRepository repositorio = new FakeProductoRepository();
        ProductoService servicio = new ProductoService(repositorio);

        Producto resultado = servicio.registrar(producto);

        assertSame(producto, resultado);
        assertSame(producto, repositorio.getUltimoProductoGuardado());
    }

    @Test
    void unProductoNuloEsRechazado() {
        FakeProductoRepository repositorio = new FakeProductoRepository();
        ProductoService servicio = new ProductoService(repositorio);

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(null));

        assertEquals("El producto no puede ser nulo.", excepcion.getMessage());
    }

    @Test
    void noSePuedeCrearElServicioSinRepositorio() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> new ProductoService(null));

        assertEquals("El repositorio de productos no puede ser nulo.", excepcion.getMessage());
    }

    private Producto crearProductoValido() {
        return new Producto("P001", "Teclado Mecánico", 500.0, 10);
    }

    private static class FakeProductoRepository implements ProductoRepository {
        private int cantidadGuardados = 0;
        private Producto ultimoProductoGuardado;

        @Override
        public Producto guardar(Producto producto) {
            this.cantidadGuardados++;
            this.ultimoProductoGuardado = producto;
            return producto;
        }

        public int getCantidadGuardados() {
            return cantidadGuardados;
        }

        public Producto getUltimoProductoGuardado() {
            return ultimoProductoGuardado;
        }
    }
}

