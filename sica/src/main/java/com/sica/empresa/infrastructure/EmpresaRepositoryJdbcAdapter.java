package com.sica.empresa.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sica.empresa.application.port.EmpresaRepositoryPort;
import com.sica.empresa.domain.Empresa;
import com.sica.infraestructura.ConexionBD;

/**
 * Adaptador de salida (hexagonal): implementa EmpresaRepositoryPort
 * usando JDBC para hablar directamente con PostgreSQL.
 */
public class EmpresaRepositoryJdbcAdapter implements EmpresaRepositoryPort {

    @Override
    public Empresa guardar(Empresa empresa) {
        String sql = "INSERT INTO empresas (nombre, identificador) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, empresa.getNombre());
            statement.setString(2, empresa.getIdentificador());

            statement.executeUpdate();

            try (ResultSet idsGenerados = statement.getGeneratedKeys()) {
                if (idsGenerados.next()) {
                    empresa.setId(idsGenerados.getLong(1));
                }
            }

            return empresa;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la empresa en la base de datos.", e);
        }
    }

    @Override
    public void actualizar(Empresa empresa) {
        String sql = "UPDATE empresas SET nombre = ?, identificador = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, empresa.getNombre());
            statement.setString(2, empresa.getIdentificador());
            statement.setLong(3, empresa.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la empresa en la base de datos.", e);
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "DELETE FROM empresas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la empresa de la base de datos.", e);
        }
    }

    @Override
    public boolean existePorId(Long id) {
        String sql = "SELECT 1 FROM empresas WHERE id = ?";
        return existe(sql, id);
    }

    private boolean existe(String sql, Long id) {
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
    public boolean existePorIdentificador(String identificador) {
        String sql = "SELECT 1 FROM empresas WHERE identificador = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, identificador);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si la empresa existe.", e);
        }
    }

    @Override
    public List<Empresa> listarTodos() {
        String sql = "SELECT id, nombre, identificador FROM empresas ORDER BY nombre";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Empresa empresa = new Empresa(resultado.getString("nombre"),
                        resultado.getString("identificador"));
                empresa.setId(resultado.getLong("id"));
                empresas.add(empresa);
            }
            return empresas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las empresas.", e);
        }
    }
}
