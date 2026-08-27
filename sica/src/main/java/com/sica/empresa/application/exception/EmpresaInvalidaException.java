package com.sica.empresa.application.exception;

/**
 * Se lanza cuando falta informacion obligatoria al registrar o actualizar una empresa.
 */
public class EmpresaInvalidaException extends RuntimeException {

    public EmpresaInvalidaException(String mensaje) {
        super(mensaje);
    }
}