package com.sica.persona.application.exception;

/**
 * Se lanza cuando se intenta asociar una empresa a una persona que no existe.
 */
public class PersonaNoEncontradaException extends RuntimeException {

    public PersonaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}