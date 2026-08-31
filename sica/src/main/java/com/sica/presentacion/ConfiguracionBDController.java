package com.sica.presentacion;

import com.sica.infraestructura.ConexionBD;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;

/** Permite configurar PostgreSQL visualmente sin guardar secretos en Git. */
public class ConfiguracionBDController {
    private final Navegador navegador;

    @FXML private TextField urlField;
    @FXML private TextField usuarioField;
    @FXML private PasswordField passwordField;
    @FXML private Label mensajeLabel;

    public ConfiguracionBDController(Navegador navegador) {
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        urlField.setText(ConexionBD.obtenerUrl());
        usuarioField.setText(ConexionBD.obtenerUsuario());
    }

    @FXML
    private void guardarYContinuar() {
        String url = urlField.getText() == null ? "" : urlField.getText().trim();
        String usuario = usuarioField.getText() == null ? "" : usuarioField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (url.isBlank() || usuario.isBlank() || password.isBlank()) {
            mostrarError("Completa la URL, el usuario tecnico y la contrasena.");
            return;
        }
        try (Connection ignored = ConexionBD.probarConexion(url, usuario, password)) {
            ConexionBD.guardarConfiguracion(url, usuario, password);
            passwordField.clear();
            navegador.mostrarLogin();
        } catch (Exception e) {
            mostrarError(ConexionBD.explicarError(e));
        }
    }

    @FXML
    private void volver() {
        navegador.mostrarLogin();
    }

    private void mostrarError(String mensaje) {
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
    }
}
