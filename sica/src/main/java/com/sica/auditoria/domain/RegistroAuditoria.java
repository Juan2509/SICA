package com.sica.auditoria.domain;

import java.time.LocalDateTime;

/**
 * Representa un registro historico de la bitacora de auditoria.
 */
public class RegistroAuditoria {

    private final Long id;
    private final String accion;
    private final String entidad;
    private final String descripcion;
    private final String usuarioResponsable;
    private final LocalDateTime fecha;
    private final String resultado;

    public RegistroAuditoria(Long id, String accion, String entidad, String descripcion,
                              String usuarioResponsable, LocalDateTime fecha, String resultado) {
        this.id = id;
        this.accion = accion;
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.usuarioResponsable = usuarioResponsable;
        this.fecha = fecha;
        this.resultado = resultado;
    }

    public Long getId() {
        return id;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getResultado() {
        return resultado;
    }
}
