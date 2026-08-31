package com.sica.presentacion;

import com.sica.usuario.domain.Usuario;
import com.sica.visita.application.VisitaService;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.application.dto.PersonaDentroInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Puesto de trabajo del guarda para validar y registrar entradas y salidas. */
public class ControlAccesoController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Usuario usuario;
    private final String nombreRol;
    private final Set<String> permisos;
    private final VisitaService visitaService;
    private final Navegador navegador;
    @FXML private TextField documentoField;
    @FXML private Label nombreLabel;
    @FXML private Label anfitrionLabel;
    @FXML private Label estadoLabel;
    @FXML private Hyperlink fotoLink;
    @FXML private Button buscarButton;
    @FXML private Button checkinButton;
    @FXML private Button checkoutButton;
    @FXML private TableView<PersonaDentroInfo> dentroTable;
    @FXML private TableColumn<PersonaDentroInfo, String> nombreColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> documentoColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> tipoColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> entradaColumn;
    @FXML private Label mensajeLabel;

    public ControlAccesoController(Usuario usuario, String nombreRol, Set<String> permisos,
                                   VisitaService visitaService, Navegador navegador) {
        this.usuario = usuario; this.nombreRol = nombreRol; this.permisos = permisos;
        this.visitaService = visitaService; this.navegador = navegador;
    }

    @FXML private void initialize() {
        dentroTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        buscarButton.setDisable(!permisos.contains("consultar_visita"));
        checkinButton.setDisable(!permisos.contains("registrar_checkin"));
        checkoutButton.setDisable(!permisos.contains("registrar_checkout"));
        nombreColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getNombre()));
        documentoColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getDocumento()));
        tipoColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getTipo().name()));
        entradaColumn.setCellValueFactory(f -> new SimpleStringProperty(
                f.getValue().getFechaHoraCheckIn() == null ? "" : f.getValue().getFechaHoraCheckIn().format(FECHA)));
        if (permisos.contains("consultar_visita")) actualizarDentro();
    }

    @FXML private void buscar() {
        try {
            DetalleVisitaConsulta d = visitaService.consultarVisitaPorDocumento(
                    documentoField.getText().trim(), usuario.getUsername());
            nombreLabel.setText(d.getNombreVisitante() + " · " + d.getDocumentoVisitante());
            anfitrionLabel.setText("Visita a: " + d.getNombrePersonaVisitada());
            estadoLabel.setText("Estado de autorización: " + d.getEstado());
            fotoLink.setText(d.getFotoUrlVisitante() == null || d.getFotoUrlVisitante().isBlank()
                    ? "Fotografía no registrada" : d.getFotoUrlVisitante());
            mostrar("Información actualizada. Verifica el estado antes de permitir el ingreso.", false);
        } catch (RuntimeException e) { limpiarDetalle(); mostrar(e.getMessage(), true); }
    }

    @FXML private void checkin() { registrar(true); }
    @FXML private void checkout() { registrar(false); }
    private void registrar(boolean entrada) {
        try {
            var visita = entrada
                    ? visitaService.registrarCheckIn(documentoField.getText().trim(), usuario.getUsername())
                    : visitaService.registrarCheckOut(documentoField.getText().trim(), usuario.getUsername());
            mostrar((entrada ? "Ingreso" : "Salida") + " registrado en la visita #" + visita.getId() + ".", false);
            buscar(); actualizarDentro();
        } catch (RuntimeException e) { mostrar(e.getMessage(), true); }
    }

    @FXML private void actualizarDentro() {
        try { dentroTable.setItems(FXCollections.observableArrayList(
                visitaService.consultarPersonasDentro(usuario.getUsername()))); }
        catch (RuntimeException e) { mostrar(e.getMessage(), true); }
    }
    private void limpiarDetalle() {
        nombreLabel.setText("Persona sin consultar"); anfitrionLabel.setText("Visita a: -");
        estadoLabel.setText("Estado de autorización: -"); fotoLink.setText("Fotografía: -");
    }
    @FXML private void volver() { navegador.mostrarPanel(usuario, nombreRol); }
    private void mostrar(String texto, boolean error) {
        mensajeLabel.setText(texto == null ? "No se pudo completar la operación." : texto);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
