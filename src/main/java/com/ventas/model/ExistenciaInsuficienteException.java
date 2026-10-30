package com.ventas.model;

/**
 * Regla de negocio violada: se intento vender mas unidades de las que hay en
 * existencia. No es un error de infraestructura; la base de datos aceptaria la
 * operacion, pero el inventario quedaria en un estado que el almacen no puede
 * surtir, asi que la venta se rechaza y la transaccion se revierte completa.
 */
public class ExistenciaInsuficienteException extends RuntimeException {

    private final String codigo;
    private final int solicitado;
    private final int disponible;

    public ExistenciaInsuficienteException(String codigo, int solicitado, int disponible) {
        super("Existencia insuficiente para el producto '" + codigo + "'. Solicitado: "
                + solicitado + ", disponible: " + disponible + ". La venta no se registro.");
        this.codigo = codigo;
        this.solicitado = solicitado;
        this.disponible = disponible;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getSolicitado() {
        return solicitado;
    }

    public int getDisponible() {
        return disponible;
    }
}
