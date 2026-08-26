package com.sica.usuario.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sica.infraestructura.ConexionBD;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;

/**
 * Adaptador de salida (hexagonal): implementa UsuarioRepositoryPort
 * usando JDBC para hablar directamente con MySQL.
 */
public class UsuarioRepositoryJdbcAdapter implements UsuarioRepositoryPort {

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, documento, username, password, rol_id, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getDocumento());
            statement.setString(3, usuario.getUsername());
            statement.setString(4, usuario.getPassword());
            statement.setLong(5, usuario.getRolId());
            statement.setBoolean(6, usuario.isActivo());

            statement.executeUpdate();

            try (ResultSet idsGenerados = statement.getGeneratedKeys()) {
                if (idsGenerados.next()) {
                    usuario.setId(idsGenerados.getLong(1));
                }
            }

            return usuario;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario en la base de datos.", e);
        }
    }

    @Override
    public boolean existePorUsername(String username) {
        String sql = "SELECT 1 FROM usuarios WHERE username = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si el usuario existe.", e);
        }
    }
}