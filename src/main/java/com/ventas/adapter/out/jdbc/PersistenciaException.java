package com.ventas.adapter.out.jdbc;

/**
 * Error de infraestructura al persistir. Envuelve la SQLException para que
 * el nucleo no tenga que conocer java.sql.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
