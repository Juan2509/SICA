package com.sica.reporte_hora.application;

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
     * Generar Reporte de Ocupación por Hora.
     */
    public List<ReporteVisitaInfo> generarReportePorHora(
        EstadoVisita estado, String usuarioResponsable) {
    autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GENERAR_REPORTE);

    if (estado == null) {
        throw new IllegalArgumentException("El estado del reporte es obligatorio.");
    }

    List<ReporteVisitaInfo> reporte = visitaRepository.listarPorHora(FechaHoraVisita).stream()
            .map(visita -> {
                Persona visitante = personaRepository.buscarPorHora(visita.getInvitadoId())
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

@param documento           documento de la persona a buscar
@param usuarioResponsable  username de quien realiza la busqueda (se valida su permiso)
@return la persona encontrada

public Persona consultarPorHora(String fechaHoraCheckIn, String usuarioResponsable) {
autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_PERSONA);

Optional<Persona> personaEncontrada = personaRepository.buscarPorDocumento(documento);

return personaEncontrada.orElseThrow(() ->
       new PersonaNoEncontradaException("No se encontraron resultados")
);
}

public List<Persona> consultarPersonas(String usuarioResponsable) {
autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_PERSONA);
return personaRepository.listarTodos();
}