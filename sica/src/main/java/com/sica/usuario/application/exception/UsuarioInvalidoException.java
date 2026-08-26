package com.sica.usuario.application.exception;

/**
 * Se lanza cuando falta informacion obligatoria al crear un usuario.
 */
public class UsuarioInvalidoException extends RuntimeException {

    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}