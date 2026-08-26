package com.sica.usuario.application.exception;

/**
 * Se lanza cuando ya existe un usuario con el mismo identificador (username).
 */
public class UsuarioDuplicadoException extends RuntimeException {

    public UsuarioDuplicadoException(String mensaje) {
        super(mensaje);
    }
}