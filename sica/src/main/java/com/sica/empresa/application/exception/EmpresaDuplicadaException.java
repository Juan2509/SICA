package com.sica.empresa.application.exception;

/**
 * Se lanza cuando ya existe una empresa registrada con el mismo identificador.
 */
public class EmpresaDuplicadaException extends RuntimeException {

    public EmpresaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}