package com.sica.auditoria.application;

import com.sica.auditoria.application.port.BitacoraConsultaPort;
import com.sica.auditoria.domain.RegistroAuditoria;
import com.sica.autorizacion.application.AutorizacionService;

import java.util.List;

/**
 * Servicio de aplicacion para consultar la trazabilidad de SICA (E9-HU02).
 */
public class AuditoriaService {

    private static final String PERMISO_CONSULTAR_BITACORA = "consultar_bitacora";

    private final BitacoraConsultaPort bitacoraConsulta;
    private final AutorizacionService autorizacionService;

    public AuditoriaService(BitacoraConsultaPort bitacoraConsulta,
                             AutorizacionService autorizacionService) {
        this.bitacoraConsulta = bitacoraConsulta;
        this.autorizacionService = autorizacionService;
    }

    public List<RegistroAuditoria> consultarBitacora(String usuarioResponsable) {
        autorizacionService.verificarPermiso(
                usuarioResponsable, PERMISO_CONSULTAR_BITACORA);

        return bitacoraConsulta.listarTodos();
    }
}
