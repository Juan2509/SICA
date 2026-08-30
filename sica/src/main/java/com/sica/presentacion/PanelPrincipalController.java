package com.sica.presentacion;

import com.sica.usuario.domain.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Panel inicial que confirma la sesion y prepara la navegacion por rol. */
public class PanelPrincipalController {

    private final Usuario usuario;
    private final String nombreRol;
    private final Navegador navegador;

    @FXML
    private Label nombreUsuarioLabel;
    @FXML
    private Label rolUsuarioLabel;
    @FXML
    private Label inicialesLabel;

    public PanelPrincipalController(Usuario usuario, String nombreRol,
                                    Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        nombreUsuarioLabel.setText(usuario.getNombre());
        rolUsuarioLabel.setText(nombreRol);
        inicialesLabel.setText(obtenerIniciales(usuario.getNombre()));
    }

    @FXML
    private void cerrarSesion() {
        navegador.mostrarLogin();
    }

    private String obtenerIniciales(String nombre) {
        StringBuilder iniciales = new StringBuilder();
        for (String parte : nombre.trim().split("\\s+")) {
            if (!parte.isEmpty() && iniciales.length() < 2) {
                iniciales.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return iniciales.toString();
    }
}
