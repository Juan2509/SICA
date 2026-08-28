package com.sica.incidente.domain;

import java.time.LocalDateTime;

/**
 * Representa una situacion de seguridad registrada en SICA.
 * La persona asociada es opcional porque no todos los incidentes
 * necesariamente involucran a una persona identificada.
 */
public class Incidente {

    private Long id;
    private String descripcion;
    private LocalDateTime fechaHora;
    private Long personaId;
    private String usuarioResponsable;

    public Incidente(String descripcion, LocalDateTime fechaHora, Long personaId,
                      String usuarioResponsable) {
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.personaId = personaId;
        this.usuarioResponsable = usuarioResponsable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }
}
