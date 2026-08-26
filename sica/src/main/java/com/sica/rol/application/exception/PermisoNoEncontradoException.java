package com.sica.rol.application.exception;

/**
 * Se lanza cuando se intenta asociar un permiso que no existe.
 */
public class PermisoNoEncontradoException extends RuntimeException {

    public PermisoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}