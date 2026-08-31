package com.sica.presentacion;

import com.sica.autenticacion.application.LoginService;
import com.sica.autenticacion.application.exception.CredencialesInvalidasException;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.usuario.domain.Usuario;
import com.sica.infraestructura.ConexionBD;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Controlador visual del login. No contiene SQL ni reglas de autenticacion. */
public class LoginController {

    private final LoginService loginService;
    private final RolRepositoryPort rolRepository;
    private final Navegador navegador;

    @FXML
    private TextField usuarioField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Button ingresarButton;

    public LoginController(LoginService loginService,
                           RolRepositoryPort rolRepository,
                           Navegador navegador) {
        this.loginService = loginService;
        this.rolRepository = rolRepository;
        this.navegador = navegador;
    }

    @FXML
    private void iniciarSesion() {
        limpiarMensaje();
        String username = usuarioField.getText() == null ? "" : usuarioField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Escribe tu usuario y contraseña.");
            return;
        }

        ingresarButton.setDisable(true);
        try {
            Usuario usuario = loginService.iniciarSesion(username, password);
            String nombreRol = rolRepository.listarRoles().stream()
                    .filter(rol -> rol.getId().equals(usuario.getRolId()))
                    .map(rol -> rol.getNombre().replace('_', ' '))
                    .findFirst()
                    .orElse("ROL SIN NOMBRE");
            navegador.mostrarPanel(usuario, nombreRol);
        } catch (CredencialesInvalidasException e) {
            passwordField.clear();
            mostrarError(e.getMessage());
        } catch (RuntimeException e) {
            mostrarError(ConexionBD.explicarError(e));
        } finally {
            ingresarButton.setDisable(false);
        }
    }

    @FXML
    private void configurarConexion() {
        navegador.mostrarConfiguracionBD();
    }

    private void limpiarMensaje() {
        mensajeLabel.setText("");
        mensajeLabel.setVisible(false);
    }

    private void mostrarError(String mensaje) {
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
    }
}
