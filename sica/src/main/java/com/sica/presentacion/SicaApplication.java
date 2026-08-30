package com.sica.presentacion;

import com.sica.auditoria.infrastructure.BitacoraAuditoriaJdbcAdapter;
import com.sica.autenticacion.application.LoginService;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.infrastructure.RolRepositoryJdbcAdapter;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.infrastructure.UsuarioRepositoryJdbcAdapter;
import javafx.application.Application;
import javafx.stage.Stage;

/** Punto de arranque y composicion de la interfaz JavaFX. */
public class SicaApplication extends Application {

    @Override
    public void start(Stage stage) {
        UsuarioRepositoryPort usuarios = new UsuarioRepositoryJdbcAdapter();
        RolRepositoryPort roles = new RolRepositoryJdbcAdapter();
        LoginService loginService = new LoginService(
                usuarios, new BitacoraAuditoriaJdbcAdapter());

        Navegador navegador = new Navegador(stage, loginService, roles);
        navegador.mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
