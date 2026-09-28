package com.ventas.model;

import java.util.List;

public class VentaFactory {

    public static Venta crearVenta(List<Partida> partidas) {
        if (partidas == null || partidas.isEmpty()) {
            throw new IllegalArgumentException("No se puede crear una venta sin partidas.");
        }

        Venta venta = new Venta();
        for (Partida partida : partidas) {
            if (partida == null) {
                throw new IllegalArgumentException("La lista contiene una partida nula.");
            }
            venta.agregarPartida(partida);
        }

        if (venta.calcularTotal() <= 0) {
            throw new IllegalArgumentException("El total de la venta debe ser mayor a cero.");
        }

        return venta;
    }
}
