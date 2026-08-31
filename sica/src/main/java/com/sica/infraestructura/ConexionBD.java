package com.sica.infraestructura;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Proporciona la conexion PostgreSQL sin guardar credenciales en el repositorio. */
public class ConexionBD {
    private static final String URL_DEFECTO = "jdbc:postgresql://localhost:5432/sica_db";
    private static final String USUARIO_DEFECTO = "sica_app";
    private static final Path ARCHIVO_LOCAL = Path.of(
            System.getProperty("user.home"), ".sica", "conexion.properties");

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(obtenerUrl(), obtenerUsuario(), obtenerPassword());
    }

    public static Connection probarConexion(String url, String usuario, String password)
            throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }

    public static boolean faltaConfiguracion() {
        return obtenerPassword().isBlank();
    }

    public static String obtenerUrl() {
        return obtenerValor("SICA_DB_URL", "url", URL_DEFECTO);
    }

    public static String obtenerUsuario() {
        return obtenerValor("SICA_DB_USER", "usuario", USUARIO_DEFECTO);
    }

    public static void guardarConfiguracion(String url, String usuario, String password)
            throws IOException {
        Properties propiedades = new Properties();
        propiedades.setProperty("url", url.trim());
        propiedades.setProperty("usuario", usuario.trim());
        propiedades.setProperty("password", password);
        Files.createDirectories(ARCHIVO_LOCAL.getParent());
        try (OutputStream salida = Files.newOutputStream(ARCHIVO_LOCAL)) {
            propiedades.store(salida, "Configuracion local de SICA - no compartir");
        }
    }

    public static String explicarError(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null) causa = causa.getCause();
        String mensaje = causa.getMessage() == null ? "" : causa.getMessage();
        String minusculas = mensaje.toLowerCase();
        if (minusculas.contains("password authentication failed")
                || minusculas.contains("autenticaci") && minusculas.contains("contrase")) {
            return "PostgreSQL rechazo la contrasena del usuario tecnico '" + obtenerUsuario() + "'.";
        }
        if (minusculas.contains("does not exist") && minusculas.contains("role")) {
            return "No existe el usuario tecnico de PostgreSQL '" + obtenerUsuario() + "'.";
        }
        if (minusculas.contains("does not exist") && minusculas.contains("database")) {
            return "No existe la base de datos sica_db.";
        }
        if (minusculas.contains("connection refused")
                || minusculas.contains("conexi") && minusculas.contains("rechaz")) {
            return "PostgreSQL no esta aceptando conexiones en localhost:5432.";
        }
        return "No se pudo conectar con PostgreSQL: " + mensaje;
    }

    private static String obtenerPassword() {
        return obtenerValor("SICA_DB_PASSWORD", "password", "");
    }

    private static String obtenerValor(String variableEntorno, String propiedad, String valorDefecto) {
        String entorno = System.getenv(variableEntorno);
        if (entorno != null && !entorno.isBlank()) return entorno;
        String local = cargarPropiedades().getProperty(propiedad);
        return local == null || local.isBlank() ? valorDefecto : local;
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        if (!Files.exists(ARCHIVO_LOCAL)) return propiedades;
        try (InputStream entrada = Files.newInputStream(ARCHIVO_LOCAL)) {
            propiedades.load(entrada);
        } catch (IOException ignored) {
            // La pantalla de configuracion permitira reemplazar un archivo invalido.
        }
        return propiedades;
    }
}
