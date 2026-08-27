package com.sica.empresa.domain;

/**
 * Representa una empresa del complejo Zona Acme.
 * El identificador (ej. NIT) identifica a la empresa de forma unica.
 */
public class Empresa {

    private Long id;
    private String nombre;
    private String identificador;

    public Empresa(String nombre, String identificador) {
        this.nombre = nombre;
        this.identificador = identificador;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}