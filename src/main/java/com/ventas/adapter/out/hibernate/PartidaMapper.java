package com.ventas.adapter.out.hibernate;

import java.math.BigDecimal;

import com.ventas.model.Partida;

public class PartidaMapper {

    private PartidaMapper() {

    }

    public static PartidaEntity toEntity(Partida partida, VentaEntity ventaEntity,
         ProductoEntity productoEntity) {
        BigDecimal precioUnitario = BigDecimal.valueOf(partida.getProducto().getPrecio());

        return new PartidaEntity(null, partida.getCantidad(), precioUnitario, 
        ventaEntity, productoEntity);
    }

    public static Partida toModelPartida(PartidaEntity partidaEntity) {
        
        return new Partida(ProductoMapper.toModel(partidaEntity.getProductoEntity()), 
        partidaEntity.getCantidad());
    }
}
