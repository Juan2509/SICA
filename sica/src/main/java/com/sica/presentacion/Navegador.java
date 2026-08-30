package com.sica.presentacion;

import com.sica.autenticacion.application.LoginService;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.usuario.domain.Usuario;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Cambia las pantallas sin mezclar esta tarea con la logica de negocio. */
public class Navegador {

    private final Stage stage;
    private final LoginService loginService;
    private final RolRepositoryPort rolRepository;

    public Navegador(Stage stage, LoginService loginService,
                     RolRepositoryPort rolRepository) {
        this.stage = stage;
        this.loginService = loginService;
        this.rolRepository = rolRepository;
    }

    public void mostrarLogin() {
        LoginController controller = new LoginController(
                loginService, rolRepository, this);
        mostrarPantalla("login.fxml", controller, "SICA | Inicio de sesión", 940, 600);
    }

    public void mostrarPanel(Usuario usuario, String nombreRol) {
        PanelPrincipalController controller = new PanelPrincipalController(
                usuario, nombreRol, this);
        mostrarPantalla("panel-principal.fxml", controller,
                "SICA | Panel principal", 1100, 680);
    }

    private void mostrarPantalla(String archivoFxml, Object controller,
                                  String titulo, double ancho, double alto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SicaApplication.class.getResource(archivoFxml));
            loader.setController(controller);
            Parent raiz = loader.load();
            Scene escena = new Scene(raiz, ancho, alto);
            escena.getStylesheets().add(
                    SicaApplication.class.getResource("estilos.css").toExternalForm());

            stage.setTitle(titulo);
            stage.setMinWidth(820);
            stage.setMinHeight(540);
            stage.setScene(escena);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("No se pudo cargar la interfaz: " + archivoFxml, e);
        }
    }
}
