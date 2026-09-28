package com.ventas.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VentaFactoryTest {

    @Test
    void testCrearVentaValidaConFactory() {
        Producto p = new Producto("P001", "Laptop", 15000.0, 10);
        Partida partida = new Partida(p, 1);
        List<Partida> partidas = List.of(partida);

        Venta venta = VentaFactory.crearVenta(partidas);

        assertNotNull(venta);
        assertEquals(15000.0, venta.calcularTotal());
    }

    @Test
    void testCrearVentaSinPartidasLanzaExcepcion() {
        List<Partida> partidasVacias = new ArrayList<>();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            VentaFactory.crearVenta(partidasVacias);
        });

        assertEquals("No se puede crear una venta sin partidas.", exception.getMessage());
    }
}
