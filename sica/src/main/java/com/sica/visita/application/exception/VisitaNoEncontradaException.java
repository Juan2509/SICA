package com.sica.visita.application.exception;

/**
 * Se lanza cuando no hay ninguna visita registrada para la persona consultada.
 */
public class VisitaNoEncontradaException extends RuntimeException {

    public VisitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}