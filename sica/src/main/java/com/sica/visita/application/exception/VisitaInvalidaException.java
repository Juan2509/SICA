package com.sica.visita.application.exception;

/**
 * Se lanza cuando falta informacion obligatoria al pre-registrar una visita.
 */
public class VisitaInvalidaException extends RuntimeException {

    public VisitaInvalidaException(String mensaje) {
        super(mensaje);
    }
}