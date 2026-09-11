package com.ventas.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VentaTest {

    @Test
    void testCreacionPartidaYSubtotal() {
        Producto p = new Producto("P001", "Teclado Mecánico", 500.0, 10);
        Partida partida = new Partida(p, 2);
        
        assertEquals(2, partida.getCantidad());
        assertEquals(1000.0, partida.getSubtotal());
    }

    @Test
    void testPartidaCantidadInvalida() {
        Producto p = new Producto("P001", "Teclado Mecánico", 500.0, 10);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Partida(p, -3);
        });
        assertEquals("La cantidad en la partida debe ser mayor a cero.", exception.getMessage());
    }

    @Test
    void testCalculoTotalVenta() {
        Producto p1 = new Producto("P001", "Mouse", 200.0, 15);
        Producto p2 = new Producto("P002", "Monitor", 3000.0, 5);

        Partida part1 = new Partida(p1, 3); // 600.0
        Partida part2 = new Partida(p2, 1); // 3000.0

        Venta venta = new Venta();
        venta.agregarPartida(part1);
        venta.agregarPartida(part2);

        assertEquals(3600.0, venta.calcularTotal());
        assertEquals(2, venta.getPartidas().size());
    }
}
