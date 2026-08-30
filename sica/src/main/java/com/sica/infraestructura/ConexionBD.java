package com.sica.infraestructura;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase sencilla para obtener conexiones PostgreSQL.
 * Las credenciales se leen de variables de entorno y no se guardan en Git.
 */
public class ConexionBD {

    private static final String URL = obtenerVariable(
            "SICA_DB_URL", "jdbc:postgresql://localhost:5432/sica_db");
    private static final String USUARIO = obtenerVariable("SICA_DB_USER", "sica_app");
    private static final String PASSWORD = obtenerVariable("SICA_DB_PASSWORD", "");

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }

    private static String obtenerVariable(String nombre, String valorPorDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? valorPorDefecto : valor;
    }
}
