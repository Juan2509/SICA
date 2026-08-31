package com.sica.presentacion;

import com.sica.empresa.application.EmpresaService;
import com.sica.empresa.domain.Empresa;
import com.sica.usuario.domain.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Controlador JavaFX para el CRUD de empresas. */
public class EmpresasController {
    private final Usuario usuario;
    private final String nombreRol;
    private final EmpresaService empresaService;
    private final Navegador navegador;
    private Long empresaSeleccionadaId;

    @FXML private TextField nombreField;
    @FXML private TextField identificadorField;
    @FXML private TableView<Empresa> empresasTable;
    @FXML private TableColumn<Empresa, String> nombreColumn;
    @FXML private TableColumn<Empresa, String> identificadorColumn;
    @FXML private Label mensajeLabel;

    public EmpresasController(Usuario usuario, String nombreRol,
                              EmpresaService empresaService, Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.empresaService = empresaService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        nombreColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombre()));
        identificadorColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getIdentificador()));
        empresasTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> cargarSeleccion(seleccionada));
        refrescarTabla();
    }

    @FXML
    private void crear() {
        ejecutar(() -> empresaService.registrarEmpresa(nombreField.getText().trim(),
                identificadorField.getText().trim(), usuario.getUsername()),
                "Empresa registrada correctamente.");
    }

    @FXML
    private void actualizar() {
        if (empresaSeleccionadaId == null) {
            mostrarMensaje("Selecciona una empresa para actualizar.", true);
            return;
        }
        ejecutar(() -> { empresaService.actualizarEmpresa(empresaSeleccionadaId,
                nombreField.getText().trim(), identificadorField.getText().trim(),
                usuario.getUsername()); return null; }, "Empresa actualizada correctamente.");
    }

    @FXML
    private void eliminar() {
        if (empresaSeleccionadaId == null) {
            mostrarMensaje("Selecciona una empresa para eliminar.", true);
            return;
        }
        ejecutar(() -> { empresaService.eliminarEmpresa(empresaSeleccionadaId,
                usuario.getUsername()); return null; }, "Empresa eliminada correctamente.");
    }

    @FXML
    private void limpiar() {
        empresaSeleccionadaId = null;
        nombreField.clear();
        identificadorField.clear();
        empresasTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }

    private void ejecutar(Operacion operacion, String mensajeExito) {
        try {
            operacion.ejecutar();
            limpiar();
            refrescarTabla();
            mostrarMensaje(mensajeExito, false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void refrescarTabla() {
        try {
            empresasTable.setItems(FXCollections.observableArrayList(
                    empresaService.consultarEmpresas(usuario.getUsername())));
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void cargarSeleccion(Empresa empresa) {
        if (empresa == null) return;
        empresaSeleccionadaId = empresa.getId();
        nombreField.setText(empresa.getNombre());
        identificadorField.setText(empresa.getIdentificador());
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "No se pudo completar la operacion." : mensaje);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }

    @FunctionalInterface
    private interface Operacion { Object ejecutar(); }
}
