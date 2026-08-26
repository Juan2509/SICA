package com.sica.auditoria.infrastructure;

import com.sica.infraestructura.ConexionBD;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Adaptador de salida (hexagonal): implementa BitacoraAuditoriaPort
 * insertando cada accion critica en la tabla bitacora_auditoria.
 */
public class BitacoraAuditoriaJdbcAdapter implements BitacoraAuditoriaPort {

    @Override
    public void registrar(String accion, String descripcion, String usuarioResponsable) {
        String sql = "INSERT INTO bitacora_auditoria (accion, descripcion, usuario_responsable, fecha) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setString(1, accion);
            statement.setString(2, descripcion);
            statement.setString(3, usuarioResponsable);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar en la bitacora de auditoria.", e);
        }
    }
}