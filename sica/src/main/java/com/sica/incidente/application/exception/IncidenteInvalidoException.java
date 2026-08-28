package com.sica.incidente.application.exception;

/**
 * Se lanza cuando faltan datos obligatorios del incidente.
 */
public class IncidenteInvalidoException extends RuntimeException {

    public IncidenteInvalidoException(String mensaje) {
        super(mensaje);
    }
}
