package com.sica.visita.application;

import java.time.LocalDateTime;
import java.util.List;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;
import com.sica.visita.application.exception.VisitaInvalidaException;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;

/**
 * Servicio de aplicacion para la Historia de Usuario E3-HU01 (Pre-registrar invitado).
 */
public class VisitaService {

    private static final String PERMISO_REGISTRAR_VISITA = "registrar_visita";
    private static final String PERMISO_CONSULTAR_VISITA = "consultar_visita";

    private final VisitaRepositoryPort visitaRepository;
    private final PersonaRepositoryPort personaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public VisitaService(VisitaRepositoryPort visitaRepository, PersonaRepositoryPort personaRepository,
                          BitacoraAuditoriaPort bitacoraAuditoria, AutorizacionService autorizacionService) {
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Pre-registra la visita de un invitado. La visita queda en estado APROBADO.
     *
     * @param invitadoId          id de la persona (invitado) que va a visitar
     * @param personaVisitadaId   id de la persona a quien se visita
     * @param fechaHoraVisita     fecha y hora programada de la visita
     * @param usuarioResponsable  username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     */
    public Visita preRegistrarInvitado(Long invitadoId, Long personaVisitadaId, LocalDateTime fechaHoraVisita,
                                        String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_REGISTRAR_VISITA);

        validarDatosObligatorios(invitadoId, personaVisitadaId, fechaHoraVisita);

        if (!personaRepository.existePorId(invitadoId)) {
            throw new PersonaNoEncontradaException("No existe una persona (invitado) con id: " + invitadoId);
        }
        if (!personaRepository.existePorId(personaVisitadaId)) {
            throw new PersonaNoEncontradaException("No existe una persona visitada con id: " + personaVisitadaId);
        }

        Visita nuevaVisita = new Visita(invitadoId, personaVisitadaId, fechaHoraVisita, EstadoVisita.APROBADO);
        Visita visitaGuardada = visitaRepository.guardar(nuevaVisita);

        bitacoraAuditoria.registrar(
                "PREREGISTRAR_VISITA",
                "Se preregistro la visita del invitado " + invitadoId + " a la persona " + personaVisitadaId,
                usuarioResponsable
        );

        return visitaGuardada;
    }

    /**
     * Permite al guarda consultar las visitas registradas de un invitado.
     */
    public List<Visita> consultarVisitasDeInvitado(Long invitadoId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_VISITA);

        if (!personaRepository.existePorId(invitadoId)) {
            throw new PersonaNoEncontradaException("No existe una persona (invitado) con id: " + invitadoId);
        }

        return visitaRepository.listarPorInvitado(invitadoId);
    }

    private void validarDatosObligatorios(Long invitadoId, Long personaVisitadaId, LocalDateTime fechaHoraVisita) {
        if (invitadoId == null) {
            throw new VisitaInvalidaException("El invitado es obligatorio.");
        }
        if (personaVisitadaId == null) {
            throw new VisitaInvalidaException("Debe indicarse a quien visita.");
        }
        if (fechaHoraVisita == null) {
            throw new VisitaInvalidaException("La fecha y hora de la visita son obligatorias.");
        }
    }
}