package com.sica.empresa.application.exception;

/**
 * Se lanza cuando se intenta actualizar, eliminar o asociar una empresa que no existe.
 */
public class EmpresaNoEncontradaException extends RuntimeException {

    public EmpresaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}