package com.ventas.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venta {
    private List<Partida> partidas;

    public Venta() {
        this.partidas = new ArrayList<>();
    }

    public void agregarPartida(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula.");
        }
        this.partidas.add(partida);
    }

    public List<Partida> getPartidas() {
        return Collections.unmodifiableList(partidas);
    }

    public double calcularTotal() {
        double total = 0.0;
        for (Partida partida : partidas) {
            total += partida.getSubtotal();
        }
        return total;
    }
}
