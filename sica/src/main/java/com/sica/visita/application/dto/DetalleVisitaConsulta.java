package com.sica.visita.application.dto;

import com.sica.visita.domain.EstadoVisita;

/**
 * Agrupa la informacion que el guarda de seguridad necesita ver al
 * consultar una visita: datos del visitante, a quien visita y el estado.
 */
public class DetalleVisitaConsulta {

    private final String nombreVisitante;
    private final String documentoVisitante;
    private final String fotoUrlVisitante;
    private final String nombrePersonaVisitada;
    private final EstadoVisita estado;

    public DetalleVisitaConsulta(String nombreVisitante, String documentoVisitante, String fotoUrlVisitante,
                                  String nombrePersonaVisitada, EstadoVisita estado) {
        this.nombreVisitante = nombreVisitante;
        this.documentoVisitante = documentoVisitante;
        this.fotoUrlVisitante = fotoUrlVisitante;
        this.nombrePersonaVisitada = nombrePersonaVisitada;
        this.estado = estado;
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

    public String getNombrePersonaVisitada() {
        return nombrePersonaVisitada;
    }

    public EstadoVisita getEstado() {
        return estado;
    }
}