package com.ventas.adapter.out.hibernate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ventas.model.Partida;
import com.ventas.model.Venta;

public class VentaMapper {

    private VentaMapper() {

    }

    public static VentaEntity toEntity(Venta venta) {
        List<PartidaEntity> partidas = new ArrayList<PartidaEntity>();
        VentaEntity ventaEntity = new VentaEntity(null, ZonedDateTime.now(),
        BigDecimal.valueOf(venta.getDescuento()),BigDecimal.valueOf(venta.calcularTotal()));
        for(Partida partida: venta.getPartidas()) {
        PartidaEntity partidaEntity = PartidaMapper.toEntity(partida, ventaEntity,
         ProductoMapper.toEntity(partida.getProducto()));

        partidas.add(partidaEntity);
        }
        ventaEntity.setPartidas(partidas);
        return ventaEntity;
    }

    public static Venta toModelVenta(VentaEntity ventaEntity) {
        List<Partida> partidas = new ArrayList<Partida>();
        for(PartidaEntity partida: ventaEntity.getPartidas()) {
            partidas.add(PartidaMapper.toModelPartida(partida));
        }
        return new Venta(partidas, ventaEntity.getDescuento().doubleValue());
    }
}
