package com.sica.auditoria.application.port;

import com.sica.auditoria.domain.RegistroAuditoria;

import java.util.List;

/**
 * Puerto de salida para consultar los registros de auditoria.
 */
public interface BitacoraConsultaPort {

    List<RegistroAuditoria> listarTodos();
}
