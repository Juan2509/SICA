package com.sica.visita.application.port;

import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita del repositorio de visitas, sin saber como esta implementado.
 */
public interface VisitaRepositoryPort {

    Visita guardar(Visita visita);

    List<Visita> listarPorInvitado(Long invitadoId);

    Optional<Visita> buscarPorId(Long visitaId);

    List<Visita> listarPendientesPorPersonaVisitada(Long personaVisitadaId);

    void actualizarEstado(Long visitaId, EstadoVisita estado);

    void registrarCheckIn(Long visitaId, LocalDateTime fechaHoraCheckIn, String usuarioCheckIn);

    void registrarCheckOut(Long visitaId, LocalDateTime fechaHoraCheckOut, String usuarioCheckOut);

    List<Visita> listarPorEstado(EstadoVisita estado);
}
