package com.ventas.application.service;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarProductosUseCaseTest {

    @Test
    void consultarTodosRetornaListaDeProductosDelRepositorio() {
        FakeProductoRepository repositorio = new FakeProductoRepository();
        repositorio.guardar(new Producto("P001", "Teclado", 300.0, 10));
        repositorio.guardar(new Producto("P002", "Mouse", 150.0, 20));

        ConsultarProductosUseCase servicio = new ProductoService(repositorio);
        List<Producto> productos = servicio.consultarTodos();

        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertEquals("P001", productos.get(0).getCodigo());
        assertEquals("P002", productos.get(1).getCodigo());
    }

    @Test
    void consultarTodosRetornaListaVaciaSiNoHayProductos() {
        FakeProductoRepository repositorio = new FakeProductoRepository();
        ConsultarProductosUseCase servicio = new ProductoService(repositorio);

        List<Producto> productos = servicio.consultarTodos();

        assertNotNull(productos);
        assertTrue(productos.isEmpty());
    }

    private static class FakeProductoRepository implements ProductoRepository {
        private final List<Producto> productos = new ArrayList<>();

        @Override
        public Producto guardar(Producto producto) {
            productos.add(producto);
            return producto;
        }

        @Override
        public List<Producto> obtenerTodos() {
            return Collections.unmodifiableList(productos);
        }
    }
}
