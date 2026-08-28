package com.sica.visita.application.dto;

import com.sica.visita.domain.EstadoVisita;

import java.time.LocalDateTime;

/**
 * Informacion que recibe el funcionario sobre una solicitud de visita.
 */
public class SolicitudAprobacionInfo {

    private final Long visitaId;
    private final String nombreVisitante;
    private final String documentoVisitante;
    private final String fotoUrlVisitante;
    private final LocalDateTime fechaHoraSolicitud;
    private final EstadoVisita estado;

    public SolicitudAprobacionInfo(Long visitaId, String nombreVisitante, String documentoVisitante,
                                   String fotoUrlVisitante, LocalDateTime fechaHoraSolicitud,
                                   EstadoVisita estado) {
        this.visitaId = visitaId;
        this.nombreVisitante = nombreVisitante;
        this.documentoVisitante = documentoVisitante;
        this.fotoUrlVisitante = fotoUrlVisitante;
        this.fechaHoraSolicitud = fechaHoraSolicitud;
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

    public String getFotoUrlVisitante() {
        return fotoUrlVisitante;
    }

    public LocalDateTime getFechaHoraSolicitud() {
        return fechaHoraSolicitud;
    }

    public EstadoVisita getEstado() {
        return estado;
    }
}
