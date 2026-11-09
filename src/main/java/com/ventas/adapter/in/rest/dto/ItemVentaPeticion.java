package com.ventas.adapter.in.rest.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Partida solicitada dentro de {@link NuevaVentaPeticion} (esquema ItemVentaPeticion de docs/openapi.yaml).
 */
public class ItemVentaPeticion {

    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "cantidad debe ser al menos 1")
    private Integer cantidad;

    public ItemVentaPeticion() {
    }

    public ItemVentaPeticion(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
