package com.sica.presentacion;

import com.sica.incidente.application.IncidenteService;
import com.sica.usuario.domain.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/** Pantalla sencilla para registrar situaciones de seguridad. */
public class IncidentesController {
    private final Usuario usuario;
    private final String nombreRol;
    private final IncidenteService incidenteService;
    private final Navegador navegador;

    @FXML private TextArea descripcionArea;
    @FXML private TextField documentoPersonaField;
    @FXML private Label mensajeLabel;

    public IncidentesController(Usuario usuario, String nombreRol,
                                IncidenteService incidenteService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.incidenteService = incidenteService;
        this.navegador = navegador;
    }

    @FXML
    private void registrar() {
        try {
            var incidente = incidenteService.registrarIncidentePorDocumento(
                    descripcionArea.getText(), documentoPersonaField.getText(), usuario.getUsername());
            descripcionArea.clear();
            documentoPersonaField.clear();
            mostrarMensaje("Incidente #" + incidente.getId()
                    + " registrado el " + incidente.getFechaHora() + ".", false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "No se pudo registrar el incidente." : mensaje);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
