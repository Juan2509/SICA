package com.sica.rol.application.exception;

/**
 * Se lanza cuando se intenta operar sobre un rol que no existe.
 */
public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}