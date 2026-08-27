package com.sica.visita.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.application.exception.AccesoNoAutorizadoException;
import com.sica.visita.application.exception.VisitaInvalidaException;
import com.sica.visita.application.exception.VisitaNoEncontradaException;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicacion para la Historia de Usuario E3-HU01 (Pre-registrar invitado).
 */
public class VisitaService {

    private static final String PERMISO_REGISTRAR_VISITA = "registrar_visita";
    private static final String PERMISO_CONSULTAR_VISITA = "consultar_visita";
    private static final String PERMISO_REGISTRAR_CHECKIN = "registrar_checkin";

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

    /**
     * Busca la visita mas reciente de una persona a partir de su documento (E3-HU02).
     * Devuelve los datos del visitante, su foto, a quien visita y el estado de la visita.
     */
    public DetalleVisitaConsulta consultarVisitaPorDocumento(String documentoVisitante, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_VISITA);

        Persona visitante = personaRepository.buscarPorDocumento(documentoVisitante)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe ninguna persona registrada con el documento: " + documentoVisitante));

        List<Visita> visitas = visitaRepository.listarPorInvitado(visitante.getId());

        if (visitas.isEmpty()) {
            throw new VisitaNoEncontradaException(
                    "No hay ninguna visita registrada para el documento: " + documentoVisitante);
        }

        Visita visitaMasReciente = visitas.get(0);

        Persona personaVisitada = personaRepository.buscarPorId(visitaMasReciente.getPersonaVisitadaId())
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe la persona visitada con id: " + visitaMasReciente.getPersonaVisitadaId()));

        return new DetalleVisitaConsulta(
                visitante.getNombre(),
                visitante.getDocumento(),
                visitante.getFotoUrl(),
                personaVisitada.getNombre(),
                visitaMasReciente.getEstado()
        );
    }

    /**
     * Registra el check-in (entrada) de una persona, a partir de su documento (E4-HU01).
     * Solo se permite si su visita mas reciente esta en estado APROBADO.
     *
     * @param documentoVisitante  documento de la persona que va a ingresar
     * @param usuarioResponsable  username del guarda que realiza el check-in (se valida su permiso y queda en la bitacora)
     */
    public Visita registrarCheckIn(String documentoVisitante, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_REGISTRAR_CHECKIN);

        Persona visitante = personaRepository.buscarPorDocumento(documentoVisitante)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe ninguna persona registrada con el documento: " + documentoVisitante));

        List<Visita> visitas = visitaRepository.listarPorInvitado(visitante.getId());

        if (visitas.isEmpty()) {
            throw new VisitaNoEncontradaException(
                    "No hay ninguna visita registrada para el documento: " + documentoVisitante);
        }

        Visita visitaMasReciente = visitas.get(0);

        if (visitaMasReciente.getEstado() != EstadoVisita.APROBADO) {
            throw new AccesoNoAutorizadoException(
                    "El ingreso no esta autorizado. Estado actual de la visita: " + visitaMasReciente.getEstado());
        }

        LocalDateTime ahora = LocalDateTime.now();
        visitaRepository.registrarCheckIn(visitaMasReciente.getId(), ahora, usuarioResponsable);

        visitaMasReciente.setEstado(EstadoVisita.DENTRO);
        visitaMasReciente.setFechaHoraCheckIn(ahora);
        visitaMasReciente.setUsuarioCheckIn(usuarioResponsable);

        bitacoraAuditoria.registrar(
                "REGISTRAR_CHECKIN",
                "Se registro el check-in de " + visitante.getNombre() + " (documento: " + documentoVisitante + ")",
                usuarioResponsable
        );

        return visitaMasReciente;
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