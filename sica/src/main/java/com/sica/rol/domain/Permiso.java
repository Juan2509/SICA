package com.sica.rol.domain;

/**
 * Representa un permiso individual del sistema (ej. crear_usuario, registrar_visita).
 * Los permisos se guardan en base de datos, nunca escritos directamente en el codigo.
 */
public class Permiso {

    private Long id;
    private String nombre;

    public Permiso(Long id, String nombre) {
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