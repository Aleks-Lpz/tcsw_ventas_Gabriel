package com.ventas.application.port.out;

import com.ventas.model.EventoDeDominio;

public interface PublicadorDeEventos {

    void publicar(EventoDeDominio evento);
}
