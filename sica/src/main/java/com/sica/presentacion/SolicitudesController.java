package com.sica.presentacion;

import com.sica.usuario.domain.Usuario;
import com.sica.visita.application.VisitaService;
import com.sica.visita.application.dto.SolicitudAprobacionInfo;
import com.sica.visita.domain.EstadoVisita;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.Set;

/** Interfaz de solicitudes compartida por guardas y funcionarios. */
public class SolicitudesController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Usuario usuario;
    private final String nombreRol;
    private final Set<String> permisos;
    private final VisitaService visitaService;
    private final Navegador navegador;
    private Timeline actualizacionAutomatica;

    @FXML private VBox noAnunciadoPanel;
    @FXML private VBox olvidoPanel;
    @FXML private VBox respuestaPanel;
    @FXML private VBox estadoPanel;
    @FXML private TextField nombreVisitanteField;
    @FXML private TextField documentoVisitanteField;
    @FXML private TextField fotoVisitanteField;
    @FXML private TextField anfitrionNoAnunciadoField;
    @FXML private TextField documentoTrabajadorField;
    @FXML private TextField funcionarioOlvidoField;
    @FXML private TextField visitaEstadoField;
    @FXML private Label estadoSolicitudLabel;
    @FXML private TableView<SolicitudAprobacionInfo> solicitudesTable;
    @FXML private TableColumn<SolicitudAprobacionInfo, String> idColumn;
    @FXML private TableColumn<SolicitudAprobacionInfo, String> visitanteColumn;
    @FXML private TableColumn<SolicitudAprobacionInfo, String> documentoColumn;
    @FXML private TableColumn<SolicitudAprobacionInfo, String> fechaColumn;
    @FXML private TableColumn<SolicitudAprobacionInfo, String> tipoColumn;
    @FXML private Button aprobarButton;
    @FXML private Button rechazarButton;
    @FXML private Label mensajeLabel;

    public SolicitudesController(Usuario usuario, String nombreRol, Set<String> permisos,
                                 VisitaService visitaService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.permisos = permisos;
        this.visitaService = visitaService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        configurarPanel(noAnunciadoPanel, permisos.contains("registrar_visitante_no_anunciado"));
        configurarPanel(olvidoPanel, permisos.contains("solicitar_ingreso_por_olvido"));
        configurarPanel(respuestaPanel, permisos.contains("responder_solicitud_visita"));
        configurarPanel(estadoPanel, permisos.contains("consultar_visita"));

        idColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getVisitaId())));
        visitanteColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreVisitante()));
        documentoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumentoVisitante()));
        fechaColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaHoraSolicitud().format(FECHA)));
        tipoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        solicitudesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> {
                    boolean sinSeleccion = seleccionada == null;
                    aprobarButton.setDisable(sinSeleccion);
                    rechazarButton.setDisable(sinSeleccion);
                });

        if (permisos.contains("responder_solicitud_visita")) refrescarPendientes(false);
        iniciarActualizacionAutomatica();
    }

    private void configurarPanel(VBox panel, boolean visible) {
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    @FXML
    private void registrarNoAnunciado() {
        try {
            var visita = visitaService.registrarVisitanteNoAnunciadoPorDocumento(
                    nombreVisitanteField.getText().trim(), documentoVisitanteField.getText().trim(),
                    textoOpcional(fotoVisitanteField.getText()),
                    anfitrionNoAnunciadoField.getText().trim(), usuario.getUsername());
            visitaEstadoField.setText(String.valueOf(visita.getId()));
            estadoSolicitudLabel.setText(visita.getEstado().name());
            mostrarMensaje("Solicitud #" + visita.getId() + " enviada al funcionario.", false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void solicitarPorOlvido() {
        try {
            var visita = visitaService.solicitarIngresoPorOlvidoPorDocumento(
                    documentoTrabajadorField.getText().trim(),
                    funcionarioOlvidoField.getText().trim(), usuario.getUsername());
            visitaEstadoField.setText(String.valueOf(visita.getId()));
            estadoSolicitudLabel.setText(visita.getEstado().name());
            mostrarMensaje("Solicitud #" + visita.getId() + " enviada al funcionario.", false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void consultarEstado() {
        actualizarEstado(true);
    }

    private void actualizarEstado(boolean mostrarErrores) {
        if (visitaEstadoField.getText() == null || visitaEstadoField.getText().isBlank()) return;
        try {
            Long visitaId = Long.valueOf(visitaEstadoField.getText().trim());
            EstadoVisita estado = visitaService.consultarEstadoSolicitud(visitaId, usuario.getUsername());
            estadoSolicitudLabel.setText(estado.name());
        } catch (NumberFormatException e) {
            if (mostrarErrores) mostrarMensaje("El ID de la solicitud debe ser numerico.", true);
        } catch (RuntimeException e) {
            if (mostrarErrores) mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void refrescarPendientes() { refrescarPendientes(true); }

    private void refrescarPendientes(boolean mostrarErrores) {
        try {
            solicitudesTable.setItems(FXCollections.observableArrayList(
                    visitaService.consultarSolicitudesPendientesPorDocumento(
                            usuario.getDocumento(), usuario.getUsername())));
        } catch (RuntimeException e) {
            if (mostrarErrores) mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void aprobar() { responder(true); }

    @FXML
    private void rechazar() { responder(false); }

    private void responder(boolean aprobar) {
        SolicitudAprobacionInfo seleccionada = solicitudesTable.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;
        try {
            if (aprobar) visitaService.aprobarSolicitud(seleccionada.getVisitaId(), usuario.getUsername());
            else visitaService.rechazarSolicitud(seleccionada.getVisitaId(), usuario.getUsername());
            mostrarMensaje("Solicitud #" + seleccionada.getVisitaId()
                    + (aprobar ? " aprobada." : " rechazada."), false);
            refrescarPendientes(false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void iniciarActualizacionAutomatica() {
        actualizacionAutomatica = new Timeline(new KeyFrame(Duration.seconds(4), evento -> {
            if (permisos.contains("consultar_visita")) actualizarEstado(false);
            if (permisos.contains("responder_solicitud_visita")) refrescarPendientes(false);
        }));
        actualizacionAutomatica.setCycleCount(Timeline.INDEFINITE);
        actualizacionAutomatica.play();
    }

    @FXML
    private void volver() {
        if (actualizacionAutomatica != null) actualizacionAutomatica.stop();
        navegador.mostrarPanel(usuario, nombreRol);
    }

    private String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "No se pudo completar la operacion." : mensaje);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
