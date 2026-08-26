package com.sica.autenticacion.application.exception;

/**
 * Se lanza cuando el username no existe o la contrasena no coincide.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}