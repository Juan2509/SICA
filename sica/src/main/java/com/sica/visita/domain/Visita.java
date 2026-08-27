package com.sica.visita.domain;

import java.time.LocalDateTime;

/**
 * Representa una visita de un invitado a una persona dentro del complejo.
 */
public class Visita {

    private Long id;
    private Long invitadoId;
    private Long personaVisitadaId;
    private LocalDateTime fechaHoraVisita;
    private EstadoVisita estado;

    public Visita(Long invitadoId, Long personaVisitadaId, LocalDateTime fechaHoraVisita, EstadoVisita estado) {
        this.invitadoId = invitadoId;
        this.personaVisitadaId = personaVisitadaId;
        this.fechaHoraVisita = fechaHoraVisita;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvitadoId() {
        return invitadoId;
    }

    public Long getPersonaVisitadaId() {
        return personaVisitadaId;
    }

    public LocalDateTime getFechaHoraVisita() {
        return fechaHoraVisita;
    }

    public EstadoVisita getEstado() {
        return estado;
    }
}