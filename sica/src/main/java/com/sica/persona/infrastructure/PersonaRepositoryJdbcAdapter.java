package com.sica.persona.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.sica.infraestructura.ConexionBD;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.EstadoAcceso;
import com.sica.persona.domain.TipoPersona;

/**
 * Adaptador de salida (hexagonal): implementa PersonaRepositoryPort
 * usando JDBC para hablar directamente con PostgreSQL.
 */
public class PersonaRepositoryJdbcAdapter implements PersonaRepositoryPort {

    @Override
    public Persona guardar(Persona persona) {
        String sql = "INSERT INTO personas (nombre, documento, tipo, foto_url) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, persona.getNombre());
            statement.setString(2, persona.getDocumento());
            statement.setString(3, persona.getTipo().name());
            statement.setString(4, persona.getFotoUrl());

            statement.executeUpdate();

            try (ResultSet idsGenerados = statement.getGeneratedKeys()) {
                if (idsGenerados.next()) {
                    persona.setId(idsGenerados.getLong(1));
                }
            }

            return persona;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la persona en la base de datos.", e);
        }
    }

    @Override
    public boolean existePorDocumento(String documento) {
        String sql = "SELECT 1 FROM personas WHERE documento = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, documento);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si la persona existe.", e);
        }
    }

    @Override
    public boolean existePorId(Long id) {
        String sql = "SELECT 1 FROM personas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si la persona existe.", e);
        }
    }

    @Override
    public void asociarEmpresa(Long personaId, Long empresaId) {
        String sql = "UPDATE personas SET empresa_id = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, empresaId);
            statement.setLong(2, personaId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al asociar la empresa a la persona.", e);
        }
    }

    @Override
    public void actualizar(Persona persona) {
        String sql = "UPDATE personas SET nombre = ?, documento = ?, tipo = ?, foto_url = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, persona.getNombre());
            statement.setString(2, persona.getDocumento());
            statement.setString(3, persona.getTipo().name());
            statement.setString(4, persona.getFotoUrl());
            statement.setLong(5, persona.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la persona.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM personas WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la persona.", e);
        }
    }

    @Override
    public void actualizarEstadoAcceso(Long personaId, EstadoAcceso estadoAcceso) {
        String sql = "UPDATE personas SET estado_acceso = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, estadoAcceso.name());
            statement.setLong(2, personaId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el estado de acceso de la persona.", e);
        }
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String documento) {
        String sql = "SELECT id, nombre, documento, tipo, empresa_id, foto_url, estado_acceso "
                + "FROM personas WHERE documento = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, documento);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearPersona(resultado));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la persona por documento.", e);
        }
    }

    @Override
    public Optional<Persona> buscarPorId(Long id) {
        String sql = "SELECT id, nombre, documento, tipo, empresa_id, foto_url, estado_acceso "
                + "FROM personas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearPersona(resultado));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la persona por id.", e);
        }
    }

    private Persona mapearPersona(ResultSet resultado) throws SQLException {
        Persona persona = new Persona(
                resultado.getString("nombre"),
                resultado.getString("documento"),
                TipoPersona.valueOf(resultado.getString("tipo"))
        );
        persona.setId(resultado.getLong("id"));
        persona.setEmpresaId(resultado.getObject("empresa_id", Long.class));
        persona.setFotoUrl(resultado.getString("foto_url"));
        persona.setEstadoAcceso(EstadoAcceso.valueOf(resultado.getString("estado_acceso")));
        return persona;
    }
}
