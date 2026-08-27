package com.sica.persona.application.exception;

/**
 * Se lanza cuando falta informacion obligatoria al registrar una persona.
 */
public class PersonaInvalidaException extends RuntimeException {

    public PersonaInvalidaException(String mensaje) {
        super(mensaje);
    }
}