package com.sica.usuario.infrastructure;

import com.sica.infraestructura.ConexionBD;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

/** Adaptador PostgreSQL del puerto de usuarios. */
public class UsuarioRepositoryJdbcAdapter implements UsuarioRepositoryPort {

    @Override
    public Usuario guardar(Usuario usuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Long personaId = buscarOCrearPersona(conexion, usuario);
                String sql = "INSERT INTO usuarios "
                        + "(persona_id, username, password, rol_id, activo) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement statement = conexion.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, personaId);
                    statement.setString(2, usuario.getUsername());
                    statement.setString(3, usuario.getPassword());
                    statement.setLong(4, usuario.getRolId());
                    statement.setBoolean(5, usuario.isActivo());
                    statement.executeUpdate();

                    try (ResultSet ids = statement.getGeneratedKeys()) {
                        if (ids.next()) {
                            usuario.setId(ids.getLong(1));
                        }
                    }
                }
                conexion.commit();
                return usuario;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el usuario en PostgreSQL.", e);
        }
    }

    private Long buscarOCrearPersona(Connection conexion, Usuario usuario) throws SQLException {
        String buscar = "SELECT id FROM personas WHERE documento = ?";
        try (PreparedStatement statement = conexion.prepareStatement(buscar)) {
            statement.setString(1, usuario.getDocumento());
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getLong("id");
                }
            }
        }

        String insertar = "INSERT INTO personas "
                + "(nombre, documento, tipo, estado_acceso) VALUES (?, ?, 'TRABAJADOR', 'HABILITADO')";
        try (PreparedStatement statement = conexion.prepareStatement(
                insertar, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getDocumento());
            statement.executeUpdate();
            try (ResultSet ids = statement.getGeneratedKeys()) {
                if (ids.next()) {
                    return ids.getLong(1);
                }
            }
        }
        throw new SQLException("PostgreSQL no devolvio el id de la persona creada.");
    }

    @Override
    public void actualizar(Usuario usuario) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Long personaId = buscarPersonaIdDelUsuario(conexion, usuario.getId());

                try (PreparedStatement statement = conexion.prepareStatement(
                        "UPDATE personas SET nombre = ?, documento = ? WHERE id = ?")) {
                    statement.setString(1, usuario.getNombre());
                    statement.setString(2, usuario.getDocumento());
                    statement.setLong(3, personaId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = conexion.prepareStatement(
                        "UPDATE usuarios SET username = ?, password = ?, rol_id = ? WHERE id = ?")) {
                    statement.setString(1, usuario.getUsername());
                    statement.setString(2, usuario.getPassword());
                    statement.setLong(3, usuario.getRolId());
                    statement.setLong(4, usuario.getId());
                    statement.executeUpdate();
                }
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el usuario en PostgreSQL.", e);
        }
    }

    private Long buscarPersonaIdDelUsuario(Connection conexion, Long usuarioId) throws SQLException {
        try (PreparedStatement statement = conexion.prepareStatement(
                "SELECT persona_id FROM usuarios WHERE id = ?")) {
            statement.setLong(1, usuarioId);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getLong("persona_id");
                }
            }
        }
        throw new SQLException("No existe el usuario con id: " + usuarioId);
    }

    @Override
    public void eliminar(Long id) {
        ejecutarActualizacion("DELETE FROM usuarios WHERE id = ?", id);
    }

    @Override
    public boolean existePorId(Long id) {
        return existe("SELECT 1 FROM usuarios WHERE id = ?", id);
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
            throw new RuntimeException("Error al verificar el usuario en PostgreSQL.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql = "SELECT u.id, p.nombre, p.documento, u.username, u.password, u.rol_id "
                + "FROM usuarios u INNER JOIN personas p ON p.id = u.persona_id "
                + "WHERE u.username = ? AND u.activo = TRUE";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                Usuario usuario = new Usuario(
                        resultado.getString("nombre"), resultado.getString("documento"),
                        resultado.getString("username"), resultado.getString("password"),
                        resultado.getLong("rol_id"));
                usuario.setId(resultado.getLong("id"));
                return Optional.of(usuario);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario en PostgreSQL.", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        String sql = "SELECT u.id, p.nombre, p.documento, u.username, u.password, u.rol_id, u.activo "
                + "FROM usuarios u INNER JOIN personas p ON p.id = u.persona_id ORDER BY u.id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Usuario usuario = new Usuario(resultado.getString("nombre"),
                        resultado.getString("documento"), resultado.getString("username"),
                        resultado.getString("password"), resultado.getLong("rol_id"));
                usuario.setId(resultado.getLong("id"));
                usuarios.add(usuario);
            }
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los usuarios en PostgreSQL.", e);
        }
    }

    private boolean existe(String sql, Long id) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el usuario en PostgreSQL.", e);
        }
    }

    private void ejecutarActualizacion(String sql, Long id) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el usuario en PostgreSQL.", e);
        }
    }
}
