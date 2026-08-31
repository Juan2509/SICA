package com.sica.presentacion;

import com.sica.auditoria.application.AuditoriaService;
import com.sica.auditoria.domain.RegistroAuditoria;
import com.sica.usuario.domain.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

/** Consulta de solo lectura de la bitacora inmutable. */
public class AuditoriaController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final Usuario usuario;
    private final String nombreRol;
    private final AuditoriaService auditoriaService;
    private final Navegador navegador;

    @FXML private TableView<RegistroAuditoria> auditoriaTable;
    @FXML private TableColumn<RegistroAuditoria, String> fechaColumn;
    @FXML private TableColumn<RegistroAuditoria, String> usuarioColumn;
    @FXML private TableColumn<RegistroAuditoria, String> accionColumn;
    @FXML private TableColumn<RegistroAuditoria, String> entidadColumn;
    @FXML private TableColumn<RegistroAuditoria, String> resultadoColumn;
    @FXML private TableColumn<RegistroAuditoria, String> descripcionColumn;
    @FXML private Label totalLabel;
    @FXML private Label mensajeLabel;

    public AuditoriaController(Usuario usuario, String nombreRol,
                               AuditoriaService auditoriaService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.auditoriaService = auditoriaService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        auditoriaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        fechaColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().format(FECHA)));
        usuarioColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuarioResponsable()));
        accionColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccion()));
        entidadColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntidad()));
        resultadoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResultado()));
        descripcionColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        actualizar();
    }

    @FXML
    private void actualizar() {
        try {
            var registros = auditoriaService.consultarBitacora(usuario.getUsername());
            auditoriaTable.setItems(FXCollections.observableArrayList(registros));
            totalLabel.setText(registros.size() + " registros encontrados");
            mensajeLabel.setText("");
        } catch (RuntimeException e) {
            mensajeLabel.setText(e.getMessage());
            mensajeLabel.getStyleClass().setAll("screen-error");
        }
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }
}
