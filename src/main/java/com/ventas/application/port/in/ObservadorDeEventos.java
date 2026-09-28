package com.ventas.application.port.in;

import com.ventas.model.EventoDeDominio;

public interface ObservadorDeEventos {

    void alRecibir(EventoDeDominio evento);
}
