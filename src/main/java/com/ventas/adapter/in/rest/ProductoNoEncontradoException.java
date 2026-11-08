package com.ventas.adapter.in.rest;

/**
 * La petición referencia un productoId que no existe en el catálogo.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long productoId) {
        super("No existe el producto " + productoId);
    }
}
