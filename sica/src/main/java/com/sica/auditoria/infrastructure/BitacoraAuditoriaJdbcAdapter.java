package com.sica.auditoria.infrastructure;

import com.sica.auditoria.application.port.BitacoraConsultaPort;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.auditoria.domain.RegistroAuditoria;
import com.sica.infraestructura.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de salida (hexagonal): implementa BitacoraAuditoriaPort
 * insertando cada accion critica en la tabla bitacora_auditoria.
 */
public class BitacoraAuditoriaJdbcAdapter implements BitacoraAuditoriaPort, BitacoraConsultaPort {

    @Override
    public void registrar(String accion, String descripcion, String usuarioResponsable) {
        String sql = "INSERT INTO bitacora_auditoria "
                + "(accion, entidad, descripcion, usuario_responsable, fecha, resultado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, accion);
            statement.setString(2, identificarEntidad(accion));
            statement.setString(3, descripcion);
            statement.setString(4, usuarioResponsable);
            statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            statement.setString(6, accion.equals("LOGIN_FALLIDO") ? "FALLIDO" : "EXITOSO");

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar en la bitacora de auditoria.", e);
        }
    }

    private String identificarEntidad(String accion) {
        if (accion.contains("USUARIO") || accion.contains("LOGIN")) {
            return "USUARIO";
        }
        if (accion.contains("PERSONA") || accion.contains("ESTADO_ACCESO")) {
            return "PERSONA";
        }
        if (accion.contains("EMPRESA")) {
            return "EMPRESA";
        }
        if (accion.contains("INCIDENTE")) {
            return "INCIDENTE";
        }
        if (accion.contains("VISITA") || accion.contains("CHECKIN")
                || accion.contains("CHECKOUT") || accion.contains("SALIDA_OLVIDADA")) {
            return "VISITA";
        }
        if (accion.contains("ROL")) {
            return "ROL";
        }
        return "SISTEMA";
    }

    @Override
    public List<RegistroAuditoria> listarTodos() {
        String sql = "SELECT id, accion, entidad, descripcion, usuario_responsable, fecha, resultado "
                + "FROM bitacora_auditoria ORDER BY fecha DESC, id DESC";

        List<RegistroAuditoria> registros = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {

            while (resultado.next()) {
                registros.add(new RegistroAuditoria(
                        resultado.getLong("id"),
                        resultado.getString("accion"),
                        resultado.getString("entidad"),
                        resultado.getString("descripcion"),
                        resultado.getString("usuario_responsable"),
                        resultado.getTimestamp("fecha").toLocalDateTime(),
                        resultado.getString("resultado")
                ));
            }

            return registros;

        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la bitacora de auditoria.", e);
        }
    }
}
