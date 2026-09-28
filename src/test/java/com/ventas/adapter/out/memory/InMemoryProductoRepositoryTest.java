package com.ventas.adapter.out.memory;

import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductoRepositoryTest {

    @Test
    void guardarUnProducto() {
        InMemoryProductoRepository repositorio = new InMemoryProductoRepository();
        Producto producto = crearProductoValido();

        Producto guardado = repositorio.guardar(producto);

        assertSame(producto, guardado);
        assertEquals(1, repositorio.obtenerTodos().size());
    }

    @Test
    void recuperarElProductoGuardado() {
        InMemoryProductoRepository repositorio = new InMemoryProductoRepository();
        Producto producto = crearProductoValido();

        repositorio.guardar(producto);

        assertSame(producto, repositorio.obtenerTodos().get(0));
    }

    @Test
    void guardarVariosProductos() {
        InMemoryProductoRepository repositorio = new InMemoryProductoRepository();
        Producto producto1 = new Producto("P001", "Teclado", 250.0, 10);
        Producto producto2 = new Producto("P002", "Mouse", 120.0, 15);

        repositorio.guardar(producto1);
        repositorio.guardar(producto2);

        List<Producto> productos = repositorio.obtenerTodos();
        assertEquals(2, productos.size());
        assertSame(producto1, productos.get(0));
        assertSame(producto2, productos.get(1));
    }

    @Test
    void obtenerTodosNoPermiteModificarLaColeccionInterna() {
        InMemoryProductoRepository repositorio = new InMemoryProductoRepository();
        Producto producto = crearProductoValido();
        repositorio.guardar(producto);

        List<Producto> productos = repositorio.obtenerTodos();

        assertThrows(UnsupportedOperationException.class,
                () -> productos.add(new Producto("P099", "Test", 1.0, 1)));
    }

    @Test
    void unProductoNuloEsRechazado() {
        InMemoryProductoRepository repositorio = new InMemoryProductoRepository();

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> repositorio.guardar(null));

        assertEquals("El producto no puede ser nulo.", excepcion.getMessage());
    }

    private Producto crearProductoValido() {
        return new Producto("P001", "Laptop", 15000.0, 5);
    }
}

