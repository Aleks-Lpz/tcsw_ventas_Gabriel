package com.ventas.adapter.in.rest.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Venta confirmada y persistida (esquema VentaRespuesta de docs/openapi.yaml).
 */
public class VentaRespuesta {

    private final Long id;
    private final OffsetDateTime fecha;
    private final double descuento;
    private final double total;
    private final List<PartidaRespuesta> partidas;

    public VentaRespuesta(Long id, OffsetDateTime fecha, double descuento, double total,
                          List<PartidaRespuesta> partidas) {
        this.id = id;
        this.fecha = fecha;
        this.descuento = descuento;
        this.total = total;
        this.partidas = partidas;
    }

    public Long getId() {
        return id;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getTotal() {
        return total;
    }

    public List<PartidaRespuesta> getPartidas() {
        return partidas;
    }
}
