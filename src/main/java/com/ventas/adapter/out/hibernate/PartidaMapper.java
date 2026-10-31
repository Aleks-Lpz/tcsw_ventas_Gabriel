package com.ventas.adapter.out.hibernate;

import com.ventas.model.Partida;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PartidaMapper {

    private PartidaMapper() {
    }

    public static PartidaEntity toEntity(Partida partida, VentaEntity ventaEntity,
                                         ProductoEntity productoEntity) {
        BigDecimal precioUnitario = BigDecimal.valueOf(partida.getProducto().getPrecio())
                .setScale(2, RoundingMode.HALF_UP);
        return new PartidaEntity(
                null,
                partida.getCantidad(),
                precioUnitario,
                ventaEntity,
                productoEntity
        );
    }

    public static Partida toModelPartida(PartidaEntity partidaEntity) {
        return new Partida(
                ProductoMapper.toModel(partidaEntity.getProductoEntity()),
                partidaEntity.getCantidad()
        );
    }
}