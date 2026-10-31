package com.ventas.adapter.out.hibernate;

import com.ventas.model.Partida;
import com.ventas.model.Venta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class VentaMapper {

    private VentaMapper() {
    }

    public static VentaEntity toEntity(Venta venta, Function<String, ProductoEntity> proveedorProducto) {
        BigDecimal descuento = dinero(venta.getDescuento());
        BigDecimal total = dinero(venta.calcularTotal());
        VentaEntity ventaEntity = new VentaEntity(null, ZonedDateTime.now(), descuento, total);

        List<PartidaEntity> partidas = new ArrayList<>();
        for (Partida partida : venta.getPartidas()) {
            ProductoEntity productoRef = proveedorProducto.apply(partida.getProducto().getCodigo());
            PartidaEntity partidaEntity = PartidaMapper.toEntity(partida, ventaEntity, productoRef);
            partidas.add(partidaEntity);
        }
        ventaEntity.setPartidas(partidas);
        return ventaEntity;
    }

    public static Venta toModelVenta(VentaEntity ventaEntity) {
        Venta venta = new Venta();
        for (PartidaEntity partidaEntity : ventaEntity.getPartidas()) {
            venta.agregarPartida(PartidaMapper.toModelPartida(partidaEntity));
        }
        venta.aplicarDescuento(ventaEntity.getDescuento().doubleValue());
        return venta;
    }

    private static BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }
}