package com.sica.presentacion;

import com.sica.reporte.application.ReporteService;
import com.sica.reporte.application.dto.ReporteVisitaInfo;
import com.sica.usuario.domain.Usuario;
import com.sica.visita.domain.EstadoVisita;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Genera reportes de visitas filtrados por estado. */
public class ReportesController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Usuario usuario;
    private final String nombreRol;
    private final ReporteService reporteService;
    private final Navegador navegador;

    @FXML private ComboBox<EstadoVisita> estadoCombo;
    @FXML private TableView<ReporteVisitaInfo> reporteTable;
    @FXML private TableColumn<ReporteVisitaInfo, String> idColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> visitanteColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> documentoColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> anfitrionColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> programadaColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> entradaColumn;
    @FXML private TableColumn<ReporteVisitaInfo, String> salidaColumn;
    @FXML private Label totalLabel;
    @FXML private Label mensajeLabel;

    public ReportesController(Usuario usuario, String nombreRol,
                              ReporteService reporteService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.reporteService = reporteService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        estadoCombo.setItems(FXCollections.observableArrayList(EstadoVisita.values()));
        estadoCombo.setValue(EstadoVisita.DENTRO);
        idColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getVisitaId())));
        visitanteColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreVisitante()));
        documentoColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDocumentoVisitante()));
        anfitrionColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombrePersonaVisitada()));
        programadaColumn.setCellValueFactory(d -> new SimpleStringProperty(formatear(d.getValue().getFechaHoraVisita())));
        entradaColumn.setCellValueFactory(d -> new SimpleStringProperty(formatear(d.getValue().getFechaHoraCheckIn())));
        salidaColumn.setCellValueFactory(d -> new SimpleStringProperty(formatear(d.getValue().getFechaHoraCheckOut())));
    }

    @FXML
    private void generar() {
        try {
            var reporte = reporteService.generarReporteVisitasPorEstado(
                    estadoCombo.getValue(), usuario.getUsername());
            reporteTable.setItems(FXCollections.observableArrayList(reporte));
            totalLabel.setText(reporte.size() + " visitas encontradas");
            mensajeLabel.setText("Reporte generado y registrado en auditoria.");
            mensajeLabel.getStyleClass().setAll("screen-success");
        } catch (RuntimeException e) {
            mensajeLabel.setText(e.getMessage());
            mensajeLabel.getStyleClass().setAll("screen-error");
        }
    }

    private String formatear(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }
}
