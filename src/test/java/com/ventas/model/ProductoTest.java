package com.ventas.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductoTest {

    @Test
    void testCreacionProductoValida() {
        Producto p = new Producto("P001", "Laptop", 15000.0, 10);
        assertEquals("P001", p.getCodigo());
        assertEquals("Laptop", p.getNombre());
        assertEquals(15000.0, p.getPrecio());
        assertEquals(10, p.getExistencia());
    }

    @Test
    void testPrecioNegativoInvalido() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Producto("P002", "Mouse", -50.0, 5);
        });
assertEquals("El precio del producto debe ser mayor a cero", exception.getMessage());    }

    @Test
    void testExistenciaNegativaInvalida() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Producto("P003", "Teclado", 300.0, -2);
        });
        assertEquals("La existencia no puede ser negativa.", exception.getMessage());
    }
}
