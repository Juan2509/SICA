package com.sica.persona.domain;

/**
 * Representa a una persona identificable por SICA: un trabajador o un invitado.
 * El documento identifica a la persona de forma unica en el sistema.
 * Una persona puede estar asociada a una empresa (empresaId).
 */
public class Persona {

    private Long id;
    private String nombre;
    private String documento;
    private TipoPersona tipo;
    private Long empresaId;

    public Persona(String nombre, String documento, TipoPersona tipo) {
        this.nombre = nombre;
        this.documento = documento;
        this.tipo = tipo;
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

    public String getDocumento() {
        return documento;
    }

    public TipoPersona getTipo() {
        return tipo;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}