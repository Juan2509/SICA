package com.sica.infraestructura;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase sencilla para obtener una conexion a la base de datos MySQL.
 * Ajusta URL, USUARIO y PASSWORD segun tu configuracion local.
 */
public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/sica_db";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "031208";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}