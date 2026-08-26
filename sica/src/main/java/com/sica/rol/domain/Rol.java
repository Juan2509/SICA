package com.sica.rol.domain;

/**
 * Representa un rol del sistema SICA (ej. ADMINISTRADOR, GUARDA_SEGURIDAD).
 * Un rol agrupa permisos que definen que puede hacer un usuario con ese rol.
 */
public class Rol {

    private Long id;
    private String nombre;

    public Rol(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}