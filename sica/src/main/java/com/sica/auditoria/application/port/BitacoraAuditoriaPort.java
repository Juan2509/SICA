package com.sica.auditoria.application.port;

/**
 * Puerto de salida para registrar acciones en la bitacora de auditoria.
 * Cualquier operacion critica exitosa debe usar este puerto.
 */
public interface BitacoraAuditoriaPort {

    void registrar(String accion, String descripcion, String usuarioResponsable);
}
