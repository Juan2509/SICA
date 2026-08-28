package com.sica.visita.infrastructure;

import com.sica.infraestructura.ConexionBD;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.domain.Visita;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida (hexagonal): implementa VisitaRepositoryPort
 * usando JDBC para hablar directamente con MySQL.
 */
public class VisitaRepositoryJdbcAdapter implements VisitaRepositoryPort {

    @Override
    public Visita guardar(Visita visita) {
        String sql = "INSERT INTO visitas (invitado_id, persona_visitada_id, fecha_hora_visita, estado) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, visita.getInvitadoId());
            statement.setLong(2, visita.getPersonaVisitadaId());
            statement.setTimestamp(3, Timestamp.valueOf(visita.getFechaHoraVisita()));
            statement.setString(4, visita.getEstado().name());

            statement.executeUpdate();

            try (ResultSet idsGenerados = statement.getGeneratedKeys()) {
                if (idsGenerados.next()) {
                    visita.setId(idsGenerados.getLong(1));
                }
            }

            return visita;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la visita en la base de datos.", e);
        }
    }

    @Override
    public List<Visita> listarPorInvitado(Long invitadoId) {
        String sql = "SELECT id, invitado_id, persona_visitada_id, fecha_hora_visita, estado, "
                + "fecha_hora_checkin, usuario_checkin, fecha_hora_checkout, usuario_checkout "
                + "FROM visitas WHERE invitado_id = ? ORDER BY fecha_hora_visita DESC";

        List<Visita> visitas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, invitadoId);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    visitas.add(mapearVisita(resultado));
                }
            }

            return visitas;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las visitas del invitado.", e);
        }
    }

    @Override
    public Optional<Visita> buscarPorId(Long visitaId) {
        String sql = "SELECT id, invitado_id, persona_visitada_id, fecha_hora_visita, estado, "
                + "fecha_hora_checkin, usuario_checkin, fecha_hora_checkout, usuario_checkout "
                + "FROM visitas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, visitaId);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearVisita(resultado));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la visita por id.", e);
        }
    }

    @Override
    public List<Visita> listarPendientesPorPersonaVisitada(Long personaVisitadaId) {
        String sql = "SELECT id, invitado_id, persona_visitada_id, fecha_hora_visita, estado, "
                + "fecha_hora_checkin, usuario_checkin, fecha_hora_checkout, usuario_checkout "
                + "FROM visitas WHERE persona_visitada_id = ? AND estado = ? ORDER BY fecha_hora_visita";

        List<Visita> visitas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, personaVisitadaId);
            statement.setString(2, EstadoVisita.PENDIENTE_APROBACION.name());

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    visitas.add(mapearVisita(resultado));
                }
            }
            return visitas;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las solicitudes pendientes.", e);
        }
    }

    @Override
    public void actualizarEstado(Long visitaId, EstadoVisita estado) {
        String sql = "UPDATE visitas SET estado = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, estado.name());
            statement.setLong(2, visitaId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el estado de la visita.", e);
        }
    }

    @Override
    public void registrarCheckIn(Long visitaId, LocalDateTime fechaHoraCheckIn, String usuarioCheckIn) {
        String sql = "UPDATE visitas SET estado = ?, fecha_hora_checkin = ?, usuario_checkin = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, EstadoVisita.DENTRO.name());
            statement.setTimestamp(2, Timestamp.valueOf(fechaHoraCheckIn));
            statement.setString(3, usuarioCheckIn);
            statement.setLong(4, visitaId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el check-in de la visita.", e);
        }
    }

    @Override
    public void registrarCheckOut(Long visitaId, LocalDateTime fechaHoraCheckOut, String usuarioCheckOut) {
        String sql = "UPDATE visitas SET estado = ?, fecha_hora_checkout = ?, usuario_checkout = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, EstadoVisita.FINALIZADA.name());
            statement.setTimestamp(2, Timestamp.valueOf(fechaHoraCheckOut));
            statement.setString(3, usuarioCheckOut);
            statement.setLong(4, visitaId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el check-out de la visita.", e);
        }
    }

    @Override
    public List<Visita> listarPorEstado(EstadoVisita estado) {
        String sql = "SELECT id, invitado_id, persona_visitada_id, fecha_hora_visita, estado, "
                + "fecha_hora_checkin, usuario_checkin, fecha_hora_checkout, usuario_checkout "
                + "FROM visitas WHERE estado = ?";

        List<Visita> visitas = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, estado.name());

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    visitas.add(mapearVisita(resultado));
                }
            }

            return visitas;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las visitas por estado.", e);
        }
    }

    private Visita mapearVisita(ResultSet resultado) throws SQLException {
        Visita visita = new Visita(
                resultado.getLong("invitado_id"),
                resultado.getLong("persona_visitada_id"),
                resultado.getTimestamp("fecha_hora_visita").toLocalDateTime(),
                EstadoVisita.valueOf(resultado.getString("estado"))
        );
        visita.setId(resultado.getLong("id"));

        Timestamp checkIn = resultado.getTimestamp("fecha_hora_checkin");
        if (checkIn != null) {
            visita.setFechaHoraCheckIn(checkIn.toLocalDateTime());
        }
        visita.setUsuarioCheckIn(resultado.getString("usuario_checkin"));

        Timestamp checkOut = resultado.getTimestamp("fecha_hora_checkout");
        if (checkOut != null) {
            visita.setFechaHoraCheckOut(checkOut.toLocalDateTime());
        }
        visita.setUsuarioCheckOut(resultado.getString("usuario_checkout"));

        return visita;
    }
}
