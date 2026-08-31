package com.sica.visita.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.EstadoAcceso;
import com.sica.persona.domain.TipoPersona;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.application.dto.PersonaDentroInfo;
import com.sica.visita.application.dto.SolicitudAprobacionInfo;
import com.sica.visita.application.exception.AccesoNoAutorizadoException;
import com.sica.visita.application.exception.VisitaInvalidaException;
import com.sica.visita.application.exception.VisitaNoEncontradaException;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicacion para la Historia de Usuario E3-HU01 (Pre-registrar invitado).
 */
public class VisitaService {

    private static final String PERMISO_REGISTRAR_VISITA = "registrar_visita";
    private static final String PERMISO_CONSULTAR_VISITA = "consultar_visita";
    private static final String PERMISO_REGISTRAR_CHECKIN = "registrar_checkin";
    private static final String PERMISO_REGISTRAR_CHECKOUT = "registrar_checkout";
    private static final String PERMISO_REGISTRAR_VISITANTE_NO_ANUNCIADO = "registrar_visitante_no_anunciado";
    private static final String PERMISO_SOLICITAR_INGRESO_POR_OLVIDO = "solicitar_ingreso_por_olvido";
    private static final String PERMISO_RESPONDER_SOLICITUD_VISITA = "responder_solicitud_visita";

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

        Persona invitado = personaRepository.buscarPorId(invitadoId)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe una persona (invitado) con id: " + invitadoId));

        if (invitado.getTipo() != TipoPersona.INVITADO) {
            throw new VisitaInvalidaException(
                    "La persona que se pre-registra debe ser de tipo INVITADO.");
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

        if (visitante.getEstadoAcceso() == EstadoAcceso.RESTRINGIDO) {
            throw new AccesoNoAutorizadoException(
                    "El ingreso no esta autorizado porque la persona tiene el acceso restringido.");
        }

        List<Visita> visitas = visitaRepository.listarPorInvitado(visitante.getId());

        if (visitas.isEmpty()) {
            throw new VisitaNoEncontradaException(
                    "No hay ninguna visita registrada para el documento: " + documentoVisitante);
        }

        Visita visitaAnteriorAbierta = visitas.stream()
                .filter(visita -> visita.getEstado() == EstadoVisita.DENTRO)
                .findFirst()
                .orElse(null);

        Visita visitaMasReciente;

        if (visitaAnteriorAbierta != null) {
            LocalDateTime fechaRegularizacion = LocalDateTime.now();
            visitaRepository.cerrarPorSistema(visitaAnteriorAbierta.getId(), fechaRegularizacion);

            visitaAnteriorAbierta.setEstado(EstadoVisita.CERRADA_POR_SISTEMA);
            visitaAnteriorAbierta.setFechaHoraCheckOut(fechaRegularizacion);
            visitaAnteriorAbierta.setUsuarioCheckOut("SISTEMA");

            bitacoraAuditoria.registrar(
                    "REGULARIZAR_SALIDA_OLVIDADA",
                    "El sistema cerro la visita " + visitaAnteriorAbierta.getId()
                            + " del documento " + documentoVisitante + " por salida olvidada",
                    usuarioResponsable
            );

            visitaMasReciente = visitas.stream()
                    .filter(visita -> visita.getEstado() == EstadoVisita.APROBADO)
                    .findFirst()
                    .orElse(null);

            if (visitaMasReciente == null) {
                Visita nuevaVisita = new Visita(
                        visitante.getId(),
                        visitaAnteriorAbierta.getPersonaVisitadaId(),
                        fechaRegularizacion,
                        EstadoVisita.APROBADO
                );
                visitaMasReciente = visitaRepository.guardar(nuevaVisita);
            }
        } else {
            visitaMasReciente = visitas.get(0);
        }

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

    /**
     * Registra el check-out (salida) de una persona, a partir de su documento (E4-HU02).
     * Localiza la visita activa (en estado DENTRO) y la cierra.
     *
     * @param documentoVisitante  documento de la persona que va a salir
     * @param usuarioResponsable  username del guarda que realiza el check-out (se valida su permiso y queda en la bitacora)
     */
    public Visita registrarCheckOut(String documentoVisitante, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_REGISTRAR_CHECKOUT);

        Persona visitante = personaRepository.buscarPorDocumento(documentoVisitante)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe ninguna persona registrada con el documento: " + documentoVisitante));

        List<Visita> visitas = visitaRepository.listarPorInvitado(visitante.getId());

        Visita visitaActiva = visitas.stream()
                .filter(visita -> visita.getEstado() == EstadoVisita.DENTRO)
                .findFirst()
                .orElseThrow(() -> new VisitaNoEncontradaException(
                        "No hay una visita activa (Dentro) para el documento: " + documentoVisitante));

        LocalDateTime ahora = LocalDateTime.now();
        visitaRepository.registrarCheckOut(visitaActiva.getId(), ahora, usuarioResponsable);

        visitaActiva.setEstado(EstadoVisita.FINALIZADA);
        visitaActiva.setFechaHoraCheckOut(ahora);
        visitaActiva.setUsuarioCheckOut(usuarioResponsable);

        bitacoraAuditoria.registrar(
                "REGISTRAR_CHECKOUT",
                "Se registro el check-out de " + visitante.getNombre() + " (documento: " + documentoVisitante + ")",
                usuarioResponsable
        );

        return visitaActiva;
    }

    /**
     * Devuelve las personas que actualmente estan dentro del complejo (E4-HU03),
     * es decir, cuya visita esta en estado DENTRO. Las visitas cerradas
     * (FINALIZADA o CERRADA_POR_SISTEMA) nunca aparecen aqui.
     */
    public List<PersonaDentroInfo> consultarPersonasDentro(String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_VISITA);

        List<Visita> visitasActivas = visitaRepository.listarPorEstado(EstadoVisita.DENTRO);

        return visitasActivas.stream()
                .map(visita -> {
                    Persona persona = personaRepository.buscarPorId(visita.getInvitadoId())
                            .orElseThrow(() -> new PersonaNoEncontradaException(
                                    "No existe la persona con id: " + visita.getInvitadoId()));
                    return new PersonaDentroInfo(
                            persona.getNombre(),
                            persona.getDocumento(),
                            persona.getTipo(),
                            visita.getFechaHoraCheckIn()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Registra un invitado que llego sin una visita programada (E5-HU01).
     * Si la persona ya existe, se usa su registro. La nueva visita queda
     * pendiente hasta que el funcionario la apruebe o rechace.
     */
    public Visita registrarVisitanteNoAnunciado(String nombre, String documento, String fotoUrl,
                                                 Long personaVisitadaId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(
                usuarioResponsable, PERMISO_REGISTRAR_VISITANTE_NO_ANUNCIADO);

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new VisitaInvalidaException("El nombre del visitante es obligatorio.");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new VisitaInvalidaException("El documento del visitante es obligatorio.");
        }
        if (personaVisitadaId == null || !personaRepository.existePorId(personaVisitadaId)) {
            throw new PersonaNoEncontradaException(
                    "No existe la persona que recibira la visita con id: " + personaVisitadaId);
        }

        Persona visitante = personaRepository.buscarPorDocumento(documento).orElse(null);

        if (visitante == null) {
            visitante = new Persona(nombre, documento, TipoPersona.INVITADO);
            visitante.setFotoUrl(fotoUrl);
            visitante = personaRepository.guardar(visitante);
        }

        Visita solicitud = new Visita(
                visitante.getId(),
                personaVisitadaId,
                LocalDateTime.now(),
                EstadoVisita.PENDIENTE_APROBACION
        );
        Visita visitaGuardada = visitaRepository.guardar(solicitud);

        bitacoraAuditoria.registrar(
                "SOLICITAR_VISITA_NO_ANUNCIADA",
                "Se creo la solicitud de visita " + visitaGuardada.getId()
                        + " para el documento: " + documento,
                usuarioResponsable
        );

        return visitaGuardada;
    }

    public Visita registrarVisitanteNoAnunciadoPorDocumento(String nombre, String documento,
                                                             String fotoUrl, String documentoVisitado,
                                                             String usuarioResponsable) {
        Persona personaVisitada = personaRepository.buscarPorDocumento(documentoVisitado)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe el funcionario con documento: " + documentoVisitado));
        return registrarVisitanteNoAnunciado(nombre, documento, fotoUrl,
                personaVisitada.getId(), usuarioResponsable);
    }

    /** Variante para la interfaz: localiza ambas personas por documento. */
    public Visita preRegistrarInvitadoPorDocumento(String documentoInvitado,
                                                    String documentoPersonaVisitada,
                                                    LocalDateTime fechaHoraVisita,
                                                    String usuarioResponsable) {
        Persona invitado = personaRepository.buscarPorDocumento(documentoInvitado)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe el invitado con documento: " + documentoInvitado));
        Persona personaVisitada = personaRepository.buscarPorDocumento(documentoPersonaVisitada)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe la persona visitada con documento: " + documentoPersonaVisitada));
        return preRegistrarInvitado(invitado.getId(), personaVisitada.getId(),
                fechaHoraVisita, usuarioResponsable);
    }

    /**
     * Solicita un ingreso excepcional para un trabajador que olvido su carnet (E5-HU02).
     * El trabajador se localiza por documento y la solicitud queda pendiente
     * hasta que el funcionario correspondiente la responda.
     */
    public Visita solicitarIngresoPorOlvido(String documentoTrabajador, Long funcionarioId,
                                             String usuarioResponsable) {
        autorizacionService.verificarPermiso(
                usuarioResponsable, PERMISO_SOLICITAR_INGRESO_POR_OLVIDO);

        if (documentoTrabajador == null || documentoTrabajador.trim().isEmpty()) {
            throw new VisitaInvalidaException("El documento del trabajador es obligatorio.");
        }

        Persona trabajador = personaRepository.buscarPorDocumento(documentoTrabajador)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe un trabajador con el documento: " + documentoTrabajador));

        if (trabajador.getTipo() != TipoPersona.TRABAJADOR) {
            throw new VisitaInvalidaException(
                    "La persona encontrada no esta registrada como trabajador.");
        }

        if (funcionarioId == null || !personaRepository.existePorId(funcionarioId)) {
            throw new PersonaNoEncontradaException(
                    "No existe el funcionario que recibira la solicitud con id: " + funcionarioId);
        }

        Visita solicitud = new Visita(
                trabajador.getId(),
                funcionarioId,
                LocalDateTime.now(),
                EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO
        );
        Visita visitaGuardada = visitaRepository.guardar(solicitud);

        bitacoraAuditoria.registrar(
                "SOLICITAR_INGRESO_POR_OLVIDO",
                "Se creo la solicitud de ingreso por olvido " + visitaGuardada.getId()
                        + " para el trabajador con documento: " + documentoTrabajador,
                usuarioResponsable
        );

        return visitaGuardada;
    }

    public Visita solicitarIngresoPorOlvidoPorDocumento(String documentoTrabajador,
                                                         String documentoFuncionario,
                                                         String usuarioResponsable) {
        Persona funcionario = personaRepository.buscarPorDocumento(documentoFuncionario)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe el funcionario con documento: " + documentoFuncionario));
        return solicitarIngresoPorOlvido(documentoTrabajador, funcionario.getId(),
                usuarioResponsable);
    }

    /**
     * Consulta las solicitudes pendientes dirigidas a un funcionario.
     */
    public List<SolicitudAprobacionInfo> consultarSolicitudesPendientes(
            Long personaVisitadaId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(
                usuarioResponsable, PERMISO_RESPONDER_SOLICITUD_VISITA);

        if (!personaRepository.existePorId(personaVisitadaId)) {
            throw new PersonaNoEncontradaException(
                    "No existe la persona visitada con id: " + personaVisitadaId);
        }

        Persona personaVisitada = personaRepository.buscarPorId(personaVisitadaId)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe la persona visitada con id: " + personaVisitadaId));
        autorizacionService.verificarUsuarioCorrespondeAPersona(
                usuarioResponsable, personaVisitada.getDocumento());

        return visitaRepository.listarPendientesPorPersonaVisitada(personaVisitadaId).stream()
                .map(visita -> {
                    Persona visitante = personaRepository.buscarPorId(visita.getInvitadoId())
                            .orElseThrow(() -> new PersonaNoEncontradaException(
                                    "No existe el visitante con id: " + visita.getInvitadoId()));

                    return new SolicitudAprobacionInfo(
                            visita.getId(),
                            visitante.getNombre(),
                            visitante.getDocumento(),
                            visitante.getFotoUrl(),
                            visita.getFechaHoraVisita(),
                            visita.getEstado()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<SolicitudAprobacionInfo> consultarSolicitudesPendientesPorDocumento(
            String documentoFuncionario, String usuarioResponsable) {
        Persona funcionario = personaRepository.buscarPorDocumento(documentoFuncionario)
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe el funcionario con documento: " + documentoFuncionario));
        return consultarSolicitudesPendientes(funcionario.getId(), usuarioResponsable);
    }

    public Visita aprobarSolicitud(Long visitaId, String usuarioResponsable) {
        return responderSolicitud(visitaId, EstadoVisita.APROBADO, usuarioResponsable);
    }

    public Visita rechazarSolicitud(Long visitaId, String usuarioResponsable) {
        return responderSolicitud(visitaId, EstadoVisita.RECHAZADO, usuarioResponsable);
    }

    /**
     * Permite al guarda consultar el estado actualizado de la solicitud.
     */
    public EstadoVisita consultarEstadoSolicitud(Long visitaId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_VISITA);

        return visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new VisitaNoEncontradaException(
                        "No existe una visita con id: " + visitaId))
                .getEstado();
    }

    private Visita responderSolicitud(Long visitaId, EstadoVisita nuevoEstado,
                                       String usuarioResponsable) {
        autorizacionService.verificarPermiso(
                usuarioResponsable, PERMISO_RESPONDER_SOLICITUD_VISITA);

        Visita visita = visitaRepository.buscarPorId(visitaId)
                .orElseThrow(() -> new VisitaNoEncontradaException(
                        "No existe una visita con id: " + visitaId));

        Persona personaVisitada = personaRepository.buscarPorId(visita.getPersonaVisitadaId())
                .orElseThrow(() -> new PersonaNoEncontradaException(
                        "No existe la persona visitada con id: " + visita.getPersonaVisitadaId()));
        autorizacionService.verificarUsuarioCorrespondeAPersona(
                usuarioResponsable, personaVisitada.getDocumento());

        if (visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION
                && visita.getEstado() != EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO) {
            throw new VisitaInvalidaException(
                    "La visita no esta pendiente de aprobacion. Estado actual: " + visita.getEstado());
        }

        visitaRepository.actualizarEstado(visitaId, nuevoEstado);
        visita.setEstado(nuevoEstado);

        bitacoraAuditoria.registrar(
                nuevoEstado == EstadoVisita.APROBADO ? "APROBAR_SOLICITUD_VISITA" : "RECHAZAR_SOLICITUD_VISITA",
                "La solicitud de visita " + visitaId + " cambio al estado " + nuevoEstado,
                usuarioResponsable
        );

        return visita;
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
