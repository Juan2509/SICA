package com.sica.visita.application.exception;

/**
 * Se lanza cuando se intenta hacer check-in de una visita que no esta
 * en un estado que permita el ingreso (por ejemplo, no esta Aprobada).
 */
public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}