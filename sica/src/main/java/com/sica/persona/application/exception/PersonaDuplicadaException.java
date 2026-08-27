package com.sica.persona.application.exception;

/**
 * Se lanza cuando ya existe una persona registrada con el mismo documento.
 */
public class PersonaDuplicadaException extends RuntimeException {

    public PersonaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}