package com.sica.visita.application;

import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.EstadoAcceso;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.TipoPersona;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.domain.Permiso;
import com.sica.rol.domain.Rol;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.application.dto.SolicitudAprobacionInfo;
import com.sica.visita.application.exception.AccesoNoAutorizadoException;
import com.sica.visita.application.exception.VisitaInvalidaException;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlujoInvitadoPreregistradoTest {

    private VisitaService visitaService;
    private PersonaRepositoryEnMemoria personaRepository;
    private VisitaRepositoryEnMemoria visitaRepository;
    private BitacoraEnMemoria bitacora;
    private Persona invitado;
    private Persona funcionario;
    private Persona trabajador;

    @BeforeEach
    @SuppressWarnings("unused")
    void prepararFlujo() {
        personaRepository = new PersonaRepositoryEnMemoria();
        visitaRepository = new VisitaRepositoryEnMemoria();
        UsuarioRepositoryEnMemoria usuarioRepository = new UsuarioRepositoryEnMemoria();
        RolRepositoryEnMemoria rolRepository = new RolRepositoryEnMemoria();
        bitacora = new BitacoraEnMemoria();

        invitado = personaRepository.agregar(
                new Persona("Invitado Prueba", "30000001", TipoPersona.INVITADO));
        invitado.setFotoUrl("https://example.com/foto-invitado.jpg");

        funcionario = personaRepository.agregar(
                new Persona("Funcionario Prueba", "10000003", TipoPersona.TRABAJADOR));

        trabajador = personaRepository.agregar(
                new Persona("Trabajador Sin Carnet", "10000004", TipoPersona.TRABAJADOR));

        usuarioRepository.agregar(
                new Usuario("Funcionario Prueba", "10000003", "funcionario", "clave", 3L));
        usuarioRepository.agregar(
                new Usuario("Guarda Prueba", "10000002", "guarda", "clave", 2L));

        rolRepository.conceder(3L, "registrar_visita");
        rolRepository.conceder(2L, "consultar_visita");
        rolRepository.conceder(2L, "registrar_checkin");
        rolRepository.conceder(2L, "registrar_checkout");
        rolRepository.conceder(2L, "registrar_visitante_no_anunciado");
        rolRepository.conceder(2L, "solicitar_ingreso_por_olvido");
        rolRepository.conceder(3L, "responder_solicitud_visita");

        AutorizacionService autorizacionService = new AutorizacionService(
                usuarioRepository, rolRepository);

        visitaService = new VisitaService(
                visitaRepository,
                personaRepository,
                bitacora,
                autorizacionService
        );
    }

    @Test
    void completaFlujoDesdePreregistroHastaCheckout() {
        Visita visita = visitaService.preRegistrarInvitado(
                invitado.getId(),
                funcionario.getId(),
                LocalDateTime.now().plusHours(1),
                "funcionario"
        );

        assertEquals(EstadoVisita.APROBADO, visita.getEstado());

        DetalleVisitaConsulta detalle = visitaService.consultarVisitaPorDocumento(
                invitado.getDocumento(), "guarda");

        assertEquals("Invitado Prueba", detalle.getNombreVisitante());
        assertEquals("Funcionario Prueba", detalle.getNombrePersonaVisitada());
        assertEquals("https://example.com/foto-invitado.jpg", detalle.getFotoUrlVisitante());
        assertEquals(EstadoVisita.APROBADO, detalle.getEstado());

        Visita ingreso = visitaService.registrarCheckIn(invitado.getDocumento(), "guarda");

        assertEquals(EstadoVisita.DENTRO, ingreso.getEstado());
        assertNotNull(ingreso.getFechaHoraCheckIn());
        assertEquals("guarda", ingreso.getUsuarioCheckIn());

        Visita salida = visitaService.registrarCheckOut(invitado.getDocumento(), "guarda");

        assertEquals(EstadoVisita.FINALIZADA, salida.getEstado());
        assertNotNull(salida.getFechaHoraCheckOut());
        assertEquals("guarda", salida.getUsuarioCheckOut());
        assertEquals(
                List.of("PREREGISTRAR_VISITA", "REGISTRAR_CHECKIN", "REGISTRAR_CHECKOUT"),
                bitacora.acciones
        );
    }

    @Test
    void noPermitePreregistrarUnTrabajadorComoInvitado() {
        assertNotNull(assertThrows(VisitaInvalidaException.class, () ->
                visitaService.preRegistrarInvitado(
                        funcionario.getId(),
                        funcionario.getId(),
                        LocalDateTime.now(),
                        "funcionario"
                )
        ));
    }

    @Test
    void permiteIngresoCuandoFuncionarioApruebaInvitadoNoAnunciado() {
        Visita solicitud = visitaService.registrarVisitanteNoAnunciado(
                "Invitado No Anunciado",
                "30000002",
                "https://example.com/foto-no-anunciado.jpg",
                funcionario.getId(),
                "guarda"
        );

        assertEquals(EstadoVisita.PENDIENTE_APROBACION, solicitud.getEstado());

        List<SolicitudAprobacionInfo> pendientes = visitaService.consultarSolicitudesPendientes(
                funcionario.getId(), "funcionario");

        assertEquals(1, pendientes.size());
        assertEquals(solicitud.getId(), pendientes.get(0).getVisitaId());
        assertEquals("Invitado No Anunciado", pendientes.get(0).getNombreVisitante());
        assertEquals("https://example.com/foto-no-anunciado.jpg",
                pendientes.get(0).getFotoUrlVisitante());

        visitaService.aprobarSolicitud(solicitud.getId(), "funcionario");

        assertEquals(EstadoVisita.APROBADO,
                visitaService.consultarEstadoSolicitud(solicitud.getId(), "guarda"));

        Visita ingreso = visitaService.registrarCheckIn("30000002", "guarda");

        assertEquals(EstadoVisita.DENTRO, ingreso.getEstado());
        assertTrue(bitacora.acciones.contains("SOLICITAR_VISITA_NO_ANUNCIADA"));
        assertTrue(bitacora.acciones.contains("APROBAR_SOLICITUD_VISITA"));
        assertTrue(bitacora.acciones.contains("REGISTRAR_CHECKIN"));
    }

    @Test
    void impideIngresoCuandoFuncionarioRechazaInvitadoNoAnunciado() {
        Visita solicitud = visitaService.registrarVisitanteNoAnunciado(
                "Invitado Rechazado",
                "30000003",
                null,
                funcionario.getId(),
                "guarda"
        );

        visitaService.rechazarSolicitud(solicitud.getId(), "funcionario");

        assertEquals(EstadoVisita.RECHAZADO,
                visitaService.consultarEstadoSolicitud(solicitud.getId(), "guarda"));
        assertNotNull(assertThrows(AccesoNoAutorizadoException.class,
                () -> visitaService.registrarCheckIn("30000003", "guarda")));
        assertTrue(bitacora.acciones.contains("RECHAZAR_SOLICITUD_VISITA"));
        assertFalse(bitacora.acciones.contains("REGISTRAR_CHECKIN"));
    }

    @Test
    void permiteIngresoPuntualCuandoFuncionarioApruebaOlvidoDeCarnet() {
        Visita solicitud = visitaService.solicitarIngresoPorOlvido(
                trabajador.getDocumento(), funcionario.getId(), "guarda");

        assertEquals(EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO, solicitud.getEstado());

        List<SolicitudAprobacionInfo> pendientes = visitaService.consultarSolicitudesPendientes(
                funcionario.getId(), "funcionario");

        assertEquals(1, pendientes.size());
        assertEquals(trabajador.getDocumento(), pendientes.get(0).getDocumentoVisitante());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO,
                pendientes.get(0).getEstado());

        visitaService.aprobarSolicitud(solicitud.getId(), "funcionario");

        assertEquals(EstadoVisita.APROBADO,
                visitaService.consultarEstadoSolicitud(solicitud.getId(), "guarda"));

        Visita ingreso = visitaService.registrarCheckIn(trabajador.getDocumento(), "guarda");

        assertEquals(EstadoVisita.DENTRO, ingreso.getEstado());
        assertTrue(bitacora.acciones.contains("SOLICITAR_INGRESO_POR_OLVIDO"));
        assertTrue(bitacora.acciones.contains("APROBAR_SOLICITUD_VISITA"));
        assertTrue(bitacora.acciones.contains("REGISTRAR_CHECKIN"));
    }

    @Test
    void impideIngresoCuandoFuncionarioRechazaOlvidoDeCarnet() {
        Visita solicitud = visitaService.solicitarIngresoPorOlvido(
                trabajador.getDocumento(), funcionario.getId(), "guarda");

        visitaService.rechazarSolicitud(solicitud.getId(), "funcionario");

        assertEquals(EstadoVisita.RECHAZADO,
                visitaService.consultarEstadoSolicitud(solicitud.getId(), "guarda"));
        assertNotNull(assertThrows(AccesoNoAutorizadoException.class,
                () -> visitaService.registrarCheckIn(trabajador.getDocumento(), "guarda")));
        assertTrue(bitacora.acciones.contains("SOLICITAR_INGRESO_POR_OLVIDO"));
        assertTrue(bitacora.acciones.contains("RECHAZAR_SOLICITUD_VISITA"));
        assertFalse(bitacora.acciones.contains("REGISTRAR_CHECKIN"));
    }

    @Test
    void regularizaSalidaOlvidadaYCreaUnNuevoIngreso() {
        Visita visitaAnterior = new Visita(
                invitado.getId(),
                funcionario.getId(),
                LocalDateTime.now().minusDays(1),
                EstadoVisita.DENTRO
        );
        visitaAnterior.setFechaHoraCheckIn(LocalDateTime.now().minusDays(1));
        visitaAnterior = visitaRepository.guardar(visitaAnterior);

        Visita nuevoIngreso = visitaService.registrarCheckIn(invitado.getDocumento(), "guarda");

        assertEquals(EstadoVisita.CERRADA_POR_SISTEMA, visitaAnterior.getEstado());
        assertNotNull(visitaAnterior.getFechaHoraCheckOut());
        assertEquals("SISTEMA", visitaAnterior.getUsuarioCheckOut());

        assertFalse(visitaAnterior.getId().equals(nuevoIngreso.getId()));
        assertEquals(EstadoVisita.DENTRO, nuevoIngreso.getEstado());
        assertNotNull(nuevoIngreso.getFechaHoraCheckIn());
        assertEquals("guarda", nuevoIngreso.getUsuarioCheckIn());
        assertEquals(2, visitaRepository.listarPorInvitado(invitado.getId()).size());

        assertEquals(
                List.of("REGULARIZAR_SALIDA_OLVIDADA", "REGISTRAR_CHECKIN"),
                bitacora.acciones
        );
    }

    private static class BitacoraEnMemoria implements BitacoraAuditoriaPort {
        private final List<String> acciones = new ArrayList<>();

        @Override
        public void registrar(String accion, String descripcion, String usuarioResponsable) {
            acciones.add(accion);
        }
    }

    private static class UsuarioRepositoryEnMemoria implements UsuarioRepositoryPort {
        private final Map<String, Usuario> usuarios = new HashMap<>();

        void agregar(Usuario usuario) {
            usuario.setId((long) usuarios.size() + 1);
            usuarios.put(usuario.getUsername(), usuario);
        }

        @Override
        public Usuario guardar(Usuario usuario) {
            agregar(usuario);
            return usuario;
        }

        @Override
        public void actualizar(Usuario usuario) {
            usuarios.put(usuario.getUsername(), usuario);
        }

        @Override
        public void eliminar(Long id) {
            usuarios.values().removeIf(usuario -> usuario.getId().equals(id));
        }

        @Override
        public boolean existePorId(Long id) {
            return usuarios.values().stream().anyMatch(usuario -> usuario.getId().equals(id));
        }

        @Override
        public boolean existePorUsername(String username) {
            return usuarios.containsKey(username);
        }

        @Override
        public Optional<Usuario> buscarPorUsername(String username) {
            return Optional.ofNullable(usuarios.get(username));
        }

        @Override
        public List<Usuario> listarTodos() {
            return new ArrayList<>(usuarios.values());
        }
    }

    private static class RolRepositoryEnMemoria implements RolRepositoryPort {
        private final Map<Long, Set<String>> permisos = new HashMap<>();

        void conceder(Long rolId, String permiso) {
            permisos.computeIfAbsent(rolId, id -> new HashSet<>()).add(permiso);
        }

        @Override
        public boolean tienePermiso(Long rolId, String codigoPermiso) {
            return permisos.getOrDefault(rolId, Set.of()).contains(codigoPermiso);
        }

        @Override
        public List<Rol> listarRoles() {
            return List.of();
        }

        @Override
        public boolean existeRolPorId(Long rolId) {
            return permisos.containsKey(rolId);
        }

        @Override
        public boolean existePermisoPorId(Long permisoId) {
            return false;
        }

        @Override
        public void asociarPermiso(Long rolId, Long permisoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Permiso> listarPermisosDeRol(Long rolId) {
            return List.of();
        }

        @Override
        public List<Permiso> listarPermisos() {
            return List.of();
        }
    }

    private static class PersonaRepositoryEnMemoria implements PersonaRepositoryPort {
        private final Map<Long, Persona> personas = new HashMap<>();
        private long siguienteId = 1L;

        Persona agregar(Persona persona) {
            persona.setId(siguienteId++);
            personas.put(persona.getId(), persona);
            return persona;
        }

        @Override
        public Persona guardar(Persona persona) {
            return agregar(persona);
        }

        @Override
        public void actualizar(Persona persona) {
            personas.put(persona.getId(), persona);
        }

        @Override
        public void eliminar(Long id) {
            personas.remove(id);
        }

        @Override
        public boolean existePorDocumento(String documento) {
            return buscarPorDocumento(documento).isPresent();
        }

        @Override
        public boolean existePorId(Long id) {
            return personas.containsKey(id);
        }

        @Override
        public void asociarEmpresa(Long personaId, Long empresaId) {
            personas.get(personaId).setEmpresaId(empresaId);
        }

        @Override
        public void actualizarEstadoAcceso(Long personaId, EstadoAcceso estadoAcceso) {
            personas.get(personaId).setEstadoAcceso(estadoAcceso);
        }

        @Override
        public Optional<Persona> buscarPorDocumento(String documento) {
            return personas.values().stream()
                    .filter(persona -> persona.getDocumento().equals(documento))
                    .findFirst();
        }

        @Override
        public Optional<Persona> buscarPorId(Long id) {
            return Optional.ofNullable(personas.get(id));
        }
    }

    private static class VisitaRepositoryEnMemoria implements VisitaRepositoryPort {
        private final List<Visita> visitas = new ArrayList<>();
        private long siguienteId = 1L;

        @Override
        public Visita guardar(Visita visita) {
            visita.setId(siguienteId++);
            visitas.add(visita);
            return visita;
        }

        @Override
        public List<Visita> listarPorInvitado(Long invitadoId) {
            return visitas.stream()
                    .filter(visita -> visita.getInvitadoId().equals(invitadoId))
                    .sorted(Comparator.comparing(Visita::getFechaHoraVisita).reversed())
                    .toList();
        }

        @Override
        public Optional<Visita> buscarPorId(Long visitaId) {
            return visitas.stream().filter(visita -> visita.getId().equals(visitaId)).findFirst();
        }

        @Override
        public List<Visita> listarPendientesPorPersonaVisitada(Long personaVisitadaId) {
            return visitas.stream()
                    .filter(visita -> visita.getPersonaVisitadaId().equals(personaVisitadaId))
                    .filter(visita -> visita.getEstado() == EstadoVisita.PENDIENTE_APROBACION
                            || visita.getEstado() == EstadoVisita.PENDIENTE_APROBACION_POR_OLVIDO)
                    .toList();
        }

        @Override
        public void actualizarEstado(Long visitaId, EstadoVisita estado) {
            buscarPorId(visitaId).ifPresent(visita -> visita.setEstado(estado));
        }

        @Override
        public void registrarCheckIn(Long visitaId, LocalDateTime fechaHoraCheckIn,
                                     String usuarioCheckIn) {
            Visita visita = buscarPorId(visitaId).orElseThrow();
            visita.setEstado(EstadoVisita.DENTRO);
            visita.setFechaHoraCheckIn(fechaHoraCheckIn);
            visita.setUsuarioCheckIn(usuarioCheckIn);
        }

        @Override
        public void registrarCheckOut(Long visitaId, LocalDateTime fechaHoraCheckOut,
                                      String usuarioCheckOut) {
            Visita visita = buscarPorId(visitaId).orElseThrow();
            visita.setEstado(EstadoVisita.FINALIZADA);
            visita.setFechaHoraCheckOut(fechaHoraCheckOut);
            visita.setUsuarioCheckOut(usuarioCheckOut);
        }

        @Override
        public void cerrarPorSistema(Long visitaId, LocalDateTime fechaHoraCierre) {
            Visita visita = buscarPorId(visitaId).orElseThrow();
            visita.setEstado(EstadoVisita.CERRADA_POR_SISTEMA);
            visita.setFechaHoraCheckOut(fechaHoraCierre);
            visita.setUsuarioCheckOut("SISTEMA");
        }

        @Override
        public List<Visita> listarPorEstado(EstadoVisita estado) {
            return visitas.stream().filter(visita -> visita.getEstado() == estado).toList();
        }
    }
}
