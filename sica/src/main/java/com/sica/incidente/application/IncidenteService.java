package com.sica.incidente.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.incidente.application.exception.IncidenteInvalidoException;
import com.sica.incidente.application.port.IncidenteRepositoryPort;
import com.sica.incidente.domain.Incidente;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;

import java.time.LocalDateTime;

/**
 * Servicio de aplicacion para registrar incidentes de seguridad (E7-HU01).
 */
public class IncidenteService {

    private static final String PERMISO_REGISTRAR_INCIDENTE = "registrar_incidente";

    private final IncidenteRepositoryPort incidenteRepository;
    private final PersonaRepositoryPort personaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public IncidenteService(IncidenteRepositoryPort incidenteRepository,
                             PersonaRepositoryPort personaRepository,
                             BitacoraAuditoriaPort bitacoraAuditoria,
                             AutorizacionService autorizacionService) {
        this.incidenteRepository = incidenteRepository;
        this.personaRepository = personaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Registra un incidente. personaId puede ser null cuando el incidente
     * no esta relacionado con una persona identificada.
     */
    public Incidente registrarIncidente(String descripcion, Long personaId,
                                         String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_REGISTRAR_INCIDENTE);

        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IncidenteInvalidoException("La descripcion del incidente es obligatoria.");
        }

        if (personaId != null && !personaRepository.existePorId(personaId)) {
            throw new PersonaNoEncontradaException(
                    "No existe una persona con id: " + personaId);
        }

        Incidente incidente = new Incidente(
                descripcion,
                LocalDateTime.now(),
                personaId,
                usuarioResponsable
        );
        Incidente incidenteGuardado = incidenteRepository.guardar(incidente);

        bitacoraAuditoria.registrar(
                "REGISTRAR_INCIDENTE",
                "Se registro el incidente con id: " + incidenteGuardado.getId(),
                usuarioResponsable
        );

        return incidenteGuardado;
    }

    /** Registra usando el documento; puede omitirse cuando no hay persona asociada. */
    public Incidente registrarIncidentePorDocumento(String descripcion, String documentoPersona,
                                                     String usuarioResponsable) {
        Long personaId = null;
        if (documentoPersona != null && !documentoPersona.trim().isEmpty()) {
            personaId = personaRepository.buscarPorDocumento(documentoPersona.trim())
                    .orElseThrow(() -> new PersonaNoEncontradaException(
                            "No existe una persona con documento: " + documentoPersona))
                    .getId();
        }
        return registrarIncidente(descripcion, personaId, usuarioResponsable);
    }
}
