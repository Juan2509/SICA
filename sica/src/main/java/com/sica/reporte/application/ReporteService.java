package com.sica.reporte.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.reporte.application.dto.ReporteVisitaInfo;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicacion para generar reportes con informacion almacenada (E9-HU01).
 */
public class ReporteService {

    private static final String PERMISO_GENERAR_REPORTE = "generar_reporte";

    private final VisitaRepositoryPort visitaRepository;
    private final PersonaRepositoryPort personaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public ReporteService(VisitaRepositoryPort visitaRepository,
                           PersonaRepositoryPort personaRepository,
                           BitacoraAuditoriaPort bitacoraAuditoria,
                           AutorizacionService autorizacionService) {
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Genera un reporte de las visitas que tienen el estado solicitado.
     */
    public List<ReporteVisitaInfo> generarReporteVisitasPorEstado(
            EstadoVisita estado, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GENERAR_REPORTE);

        if (estado == null) {
            throw new IllegalArgumentException("El estado del reporte es obligatorio.");
        }

        List<ReporteVisitaInfo> reporte = visitaRepository.listarPorEstado(estado).stream()
                .map(visita -> {
                    Persona visitante = personaRepository.buscarPorId(visita.getInvitadoId())
                            .orElseThrow(() -> new PersonaNoEncontradaException(
                                    "No existe el visitante con id: " + visita.getInvitadoId()));

                    Persona personaVisitada = personaRepository.buscarPorId(visita.getPersonaVisitadaId())
                            .orElseThrow(() -> new PersonaNoEncontradaException(
                                    "No existe la persona visitada con id: "
                                            + visita.getPersonaVisitadaId()));

                    return new ReporteVisitaInfo(
                            visita.getId(),
                            visitante.getNombre(),
                            visitante.getDocumento(),
                            personaVisitada.getNombre(),
                            visita.getFechaHoraVisita(),
                            visita.getFechaHoraCheckIn(),
                            visita.getFechaHoraCheckOut(),
                            visita.getEstado()
                    );
                })
                .collect(Collectors.toList());

        bitacoraAuditoria.registrar(
                "GENERAR_REPORTE",
                "Se genero un reporte de visitas con estado " + estado
                        + ". Registros encontrados: " + reporte.size(),
                usuarioResponsable
        );

        return reporte;
    }
}
