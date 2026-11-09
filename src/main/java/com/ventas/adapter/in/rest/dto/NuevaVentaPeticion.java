package com.ventas.adapter.in.rest.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Cuerpo de POST /ventas (esquema NuevaVentaPeticion de docs/openapi.yaml).
 */
public class NuevaVentaPeticion {

    @NotNull(message = "partidas es obligatorio")
    @NotEmpty(message = "la venta debe tener al menos una partida")
    private List<@NotNull(message = "una partida no puede ser nula") @Valid ItemVentaPeticion> partidas;

    public NuevaVentaPeticion() {
    }

    public NuevaVentaPeticion(List<ItemVentaPeticion> partidas) {
        this.partidas = partidas;
    }

    public List<ItemVentaPeticion> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<ItemVentaPeticion> partidas) {
        this.partidas = partidas;
    }
}
