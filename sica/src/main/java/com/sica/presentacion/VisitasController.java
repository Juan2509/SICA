package com.sica.presentacion;

import com.sica.usuario.domain.Usuario;
import com.sica.visita.application.VisitaService;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.application.dto.PersonaDentroInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

/** Pantalla JavaFX para preregistro, consulta, entrada y salida de visitas. */
public class VisitasController {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Usuario usuario;
    private final String nombreRol;
    private final Set<String> permisos;
    private final VisitaService visitaService;
    private final Navegador navegador;

    @FXML private TextField documentoInvitadoField;
    @FXML private TextField documentoAnfitrionField;
    @FXML private DatePicker fechaVisitaPicker;
    @FXML private TextField horaVisitaField;
    @FXML private Button preregistrarButton;
    @FXML private TextField documentoConsultaField;
    @FXML private Button consultarButton;
    @FXML private Label detalleNombreLabel;
    @FXML private Label detalleAnfitrionLabel;
    @FXML private Label detalleEstadoLabel;
    @FXML private Label detalleFotoLabel;
    @FXML private TextField documentoAccesoField;
    @FXML private Button checkinButton;
    @FXML private Button checkoutButton;
    @FXML private Button dentroButton;
    @FXML private TableView<PersonaDentroInfo> dentroTable;
    @FXML private TableColumn<PersonaDentroInfo, String> dentroNombreColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> dentroDocumentoColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> dentroTipoColumn;
    @FXML private TableColumn<PersonaDentroInfo, String> dentroHoraColumn;
    @FXML private Label mensajeLabel;

    public VisitasController(Usuario usuario, String nombreRol, Set<String> permisos,
                             VisitaService visitaService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.permisos = permisos;
        this.visitaService = visitaService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        fechaVisitaPicker.setValue(LocalDate.now());
        horaVisitaField.setText(LocalTime.now().plusHours(1).format(HORA));
        preregistrarButton.setDisable(!permisos.contains("registrar_visita"));
        consultarButton.setDisable(!permisos.contains("consultar_visita"));
        checkinButton.setDisable(!permisos.contains("registrar_checkin"));
        checkoutButton.setDisable(!permisos.contains("registrar_checkout"));
        dentroButton.setDisable(!permisos.contains("consultar_visita"));

        dentroNombreColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        dentroDocumentoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumento()));
        dentroTipoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        dentroHoraColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaHoraCheckIn() == null ? "" :
                        d.getValue().getFechaHoraCheckIn().format(FECHA_HORA)));
        if (permisos.contains("consultar_visita")) consultarDentro();
    }

    @FXML
    private void preregistrar() {
        try {
            LocalDateTime fechaHora = LocalDateTime.of(fechaVisitaPicker.getValue(),
                    LocalTime.parse(horaVisitaField.getText().trim(), HORA));
            var visita = visitaService.preRegistrarInvitadoPorDocumento(
                    documentoInvitadoField.getText().trim(),
                    documentoAnfitrionField.getText().trim(), fechaHora,
                    usuario.getUsername());
            mostrarMensaje("Visita #" + visita.getId() + " preregistrada como APROBADO.", false);
            documentoInvitadoField.clear();
            documentoAnfitrionField.clear();
        } catch (DateTimeParseException | NullPointerException e) {
            mostrarMensaje("Escribe una fecha y una hora validas en formato HH:mm.", true);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void consultar() {
        try {
            DetalleVisitaConsulta detalle = visitaService.consultarVisitaPorDocumento(
                    documentoConsultaField.getText().trim(), usuario.getUsername());
            detalleNombreLabel.setText(detalle.getNombreVisitante()
                    + " - " + detalle.getDocumentoVisitante());
            detalleAnfitrionLabel.setText("Visita a: " + detalle.getNombrePersonaVisitada());
            detalleEstadoLabel.setText("Estado: " + detalle.getEstado());
            detalleFotoLabel.setText("Foto: " + (detalle.getFotoUrlVisitante() == null
                    ? "No registrada" : detalle.getFotoUrlVisitante()));
            mostrarMensaje("Visita encontrada.", false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void registrarCheckIn() {
        ejecutarAcceso(true);
    }

    @FXML
    private void registrarCheckOut() {
        ejecutarAcceso(false);
    }

    private void ejecutarAcceso(boolean entrada) {
        try {
            String documento = documentoAccesoField.getText().trim();
            var visita = entrada
                    ? visitaService.registrarCheckIn(documento, usuario.getUsername())
                    : visitaService.registrarCheckOut(documento, usuario.getUsername());
            mostrarMensaje((entrada ? "Check-in" : "Check-out")
                    + " registrado para la visita #" + visita.getId() + ".", false);
            consultarDentro();
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void consultarDentro() {
        try {
            dentroTable.setItems(FXCollections.observableArrayList(
                    visitaService.consultarPersonasDentro(usuario.getUsername())));
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "No se pudo completar la operacion." : mensaje);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
