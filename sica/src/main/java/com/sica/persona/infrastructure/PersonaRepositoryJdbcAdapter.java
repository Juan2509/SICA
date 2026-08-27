package com.sica.persona.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sica.infraestructura.ConexionBD;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;

/**
 * Adaptador de salida (hexagonal): implementa PersonaRepositoryPort
 * usando JDBC para hablar directamente con MySQL.
 */
public class PersonaRepositoryJdbcAdapter implements PersonaRepositoryPort {

    @Override
    public Persona guardar(Persona persona) {
        String sql = "INSERT INTO personas (nombre, documento, tipo) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, persona.getNombre());
            statement.setString(2, persona.getDocumento());
            statement.setString(3, persona.getTipo().name());

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
}