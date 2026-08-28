package com.sica.reporte.application.dto;

import com.sica.visita.domain.EstadoVisita;

import java.time.LocalDateTime;

/**
 * Informacion de una visita incluida en un reporte.
 */
public class ReporteVisitaInfo {

    private final Long visitaId;
    private final String nombreVisitante;
    private final String documentoVisitante;
    private final String nombrePersonaVisitada;
    private final LocalDateTime fechaHoraVisita;
    private final LocalDateTime fechaHoraCheckIn;
    private final LocalDateTime fechaHoraCheckOut;
    private final EstadoVisita estado;

    public ReporteVisitaInfo(Long visitaId, String nombreVisitante, String documentoVisitante,
                             String nombrePersonaVisitada, LocalDateTime fechaHoraVisita,
                             LocalDateTime fechaHoraCheckIn, LocalDateTime fechaHoraCheckOut,
                             EstadoVisita estado) {
        this.visitaId = visitaId;
        this.nombreVisitante = nombreVisitante;
        this.documentoVisitante = documentoVisitante;
        this.nombrePersonaVisitada = nombrePersonaVisitada;
        this.fechaHoraVisita = fechaHoraVisita;
        this.fechaHoraCheckIn = fechaHoraCheckIn;
        this.fechaHoraCheckOut = fechaHoraCheckOut;
        this.estado = estado;
    }

    public Long getVisitaId() {
        return visitaId;
    }

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public String getDocumentoVisitante() {
        return documentoVisitante;
    }

    public String getNombrePersonaVisitada() {
        return nombrePersonaVisitada;
    }

    public LocalDateTime getFechaHoraVisita() {
        return fechaHoraVisita;
    }

    public LocalDateTime getFechaHoraCheckIn() {
        return fechaHoraCheckIn;
    }

    public LocalDateTime getFechaHoraCheckOut() {
        return fechaHoraCheckOut;
    }

    public EstadoVisita getEstado() {
        return estado;
    }
}
