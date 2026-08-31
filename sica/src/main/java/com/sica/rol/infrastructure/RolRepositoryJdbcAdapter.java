package com.sica.rol.infrastructure;

import com.sica.infraestructura.ConexionBD;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.domain.Permiso;
import com.sica.rol.domain.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de salida (hexagonal): implementa RolRepositoryPort
 * usando JDBC para hablar directamente con PostgreSQL.
 */
public class RolRepositoryJdbcAdapter implements RolRepositoryPort {

    @Override
    public List<Rol> listarRoles() {
        String sql = "SELECT id, nombre FROM roles";
        List<Rol> roles = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {
                roles.add(new Rol(resultado.getLong("id"), resultado.getString("nombre")));
            }

            return roles;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los roles.", e);
        }
    }

    @Override
    public boolean existeRolPorId(Long rolId) {
        String sql = "SELECT 1 FROM roles WHERE id = ?";
        return existePorId(sql, rolId);
    }

    @Override
    public boolean existePermisoPorId(Long permisoId) {
        String sql = "SELECT 1 FROM permisos WHERE id = ?";
        return existePorId(sql, permisoId);
    }

    private boolean existePorId(String sql, Long id) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia en la base de datos.", e);
        }
    }

    @Override
    public void asociarPermiso(Long rolId, Long permisoId) {
        String sql = "INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, rolId);
            statement.setLong(2, permisoId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al asociar el permiso al rol.", e);
        }
    }

    @Override
    public List<Permiso> listarPermisosDeRol(Long rolId) {
        String sql = "SELECT p.id, p.nombre "
                + "FROM permisos p "
                + "INNER JOIN rol_permiso rp ON rp.permiso_id = p.id "
                + "WHERE rp.rol_id = ?";

        List<Permiso> permisos = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, rolId);

            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    permisos.add(new Permiso(resultado.getLong("id"), resultado.getString("nombre")));
                }
            }

            return permisos;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los permisos del rol.", e);
        }
    }

    @Override
    public List<Permiso> listarPermisos() {
        List<Permiso> permisos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(
                     "SELECT id, nombre FROM permisos ORDER BY nombre");
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                permisos.add(new Permiso(resultado.getLong("id"), resultado.getString("nombre")));
            }
            return permisos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los permisos.", e);
        }
    }

    @Override
    public boolean tienePermiso(Long rolId, String codigoPermiso) {
        String sql = "SELECT 1 "
                + "FROM rol_permiso rp "
                + "INNER JOIN permisos p ON p.id = rp.permiso_id "
                + "WHERE rp.rol_id = ? AND p.nombre = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, rolId);
            statement.setString(2, codigoPermiso);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar el permiso del rol.", e);
        }
    }
}
