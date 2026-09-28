package com.ventas.application.service;

import com.ventas.application.port.in.ObservadorDeEventos;
import com.ventas.application.port.out.PublicadorDeEventos;
import com.ventas.model.EventoDeDominio;

import java.util.ArrayList;
import java.util.List;

public class PublicadorSimple implements PublicadorDeEventos {

    private final List<ObservadorDeEventos> observadores = new ArrayList<>();

    public void agregarObservador(ObservadorDeEventos observador) {
        if (observador == null) {
            throw new IllegalArgumentException("El observador no puede ser nulo.");
        }
        observadores.add(observador);
    }

    @Override
    public void publicar(EventoDeDominio evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        for (ObservadorDeEventos observador : observadores) {
            observador.alRecibir(evento);
        }
    }
}
