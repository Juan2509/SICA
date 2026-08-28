package com.sica.visita.application.dto;

import com.sica.persona.domain.TipoPersona;

import java.time.LocalDateTime;

/**
 * Representa a una persona que actualmente se encuentra dentro del complejo
 * (E4-HU03), con los datos necesarios para identificarla ante una emergencia.
 */
public class PersonaDentroInfo {

    private final String nombre;
    private final String documento;
    private final TipoPersona tipo;
    private final LocalDateTime fechaHoraCheckIn;

    public PersonaDentroInfo(String nombre, String documento, TipoPersona tipo, LocalDateTime fechaHoraCheckIn) {
        this.nombre = nombre;
        this.documento = documento;
        this.tipo = tipo;
        this.fechaHoraCheckIn = fechaHoraCheckIn;
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

    public LocalDateTime getFechaHoraCheckIn() {
        return fechaHoraCheckIn;
    }
}