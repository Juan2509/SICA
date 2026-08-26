package com.sica.autorizacion.application.exception;

/**
 * Se lanza cuando un usuario intenta ejecutar una operacion critica
 * sin tener el permiso requerido para hacerlo.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}