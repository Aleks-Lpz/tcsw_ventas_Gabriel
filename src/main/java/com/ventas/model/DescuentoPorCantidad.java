package com.ventas.model;

public class DescuentoPorCantidad implements PoliticaDescuento {

@Override 
public double calcularDescuento(Venta venta) {
    if(venta.getPartidas().size()>10) {
        return venta.calcularTotal()* 0.15;
        
    }
        throw new IllegalArgumentException("La venta no cuenta con más de 10 partidas");
}
}
