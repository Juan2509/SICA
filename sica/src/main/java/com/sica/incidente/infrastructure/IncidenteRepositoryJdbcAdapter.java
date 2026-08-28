package com.sica.incidente.infrastructure;

import com.sica.incidente.application.port.IncidenteRepositoryPort;
import com.sica.incidente.domain.Incidente;
import com.sica.infraestructura.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Adaptador JDBC para guardar incidentes en MySQL.
 */
public class IncidenteRepositoryJdbcAdapter implements IncidenteRepositoryPort {

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = "INSERT INTO incidentes (descripcion, fecha_hora, persona_id, usuario_responsable) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, incidente.getDescripcion());
            statement.setTimestamp(2, Timestamp.valueOf(incidente.getFechaHora()));

            if (incidente.getPersonaId() == null) {
                statement.setNull(3, Types.BIGINT);
            } else {
                statement.setLong(3, incidente.getPersonaId());
            }

            statement.setString(4, incidente.getUsuarioResponsable());
            statement.executeUpdate();

            try (ResultSet idsGenerados = statement.getGeneratedKeys()) {
                if (idsGenerados.next()) {
                    incidente.setId(idsGenerados.getLong(1));
                }
            }

            return incidente;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el incidente en la base de datos.", e);
        }
    }
}
