package com.sica.presentacion;

import com.sica.persona.application.PersonaService;
import com.sica.empresa.application.EmpresaService;
import com.sica.empresa.domain.Empresa;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.TipoPersona;
import com.sica.usuario.domain.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.Set;

/** Controlador JavaFX para registrar, consultar, actualizar y eliminar personas. */
public class PersonasController {
    private final Usuario usuario;
    private final String nombreRol;
    private final Set<String> permisos;
    private final PersonaService personaService;
    private final EmpresaService empresaService;
    private final Navegador navegador;
    private String documentoSeleccionado;

    @FXML private TextField nombreField;
    @FXML private TextField documentoField;
    @FXML private TextField fotoUrlField;
    @FXML private ComboBox<TipoPersona> tipoCombo;
    @FXML private ComboBox<Empresa> empresaCombo;
    @FXML private TableView<Persona> personasTable;
    @FXML private TableColumn<Persona, String> nombreColumn;
    @FXML private TableColumn<Persona, String> documentoColumn;
    @FXML private TableColumn<Persona, String> tipoColumn;
    @FXML private TableColumn<Persona, String> estadoColumn;
    @FXML private TableColumn<Persona, String> empresaColumn;
    @FXML private Button crearButton;
    @FXML private Button actualizarButton;
    @FXML private Button eliminarButton;
    @FXML private Button buscarButton;
    @FXML private Label mensajeLabel;

    public PersonasController(Usuario usuario, String nombreRol, Set<String> permisos,
                              PersonaService personaService, EmpresaService empresaService,
                              Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.permisos = permisos;
        this.personaService = personaService;
        this.empresaService = empresaService;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        tipoCombo.setItems(FXCollections.observableArrayList(TipoPersona.values()));
        empresaCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Empresa empresa) {
                return empresa == null ? "" : empresa.getNombre();
            }
            @Override public Empresa fromString(String texto) { return null; }
        });
        nombreColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getNombre()));
        documentoColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getDocumento()));
        tipoColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getTipo().name()));
        estadoColumn.setCellValueFactory(dato -> new SimpleStringProperty(dato.getValue().getEstadoAcceso().name()));
        empresaColumn.setCellValueFactory(dato -> new SimpleStringProperty(
                dato.getValue().getEmpresaId() == null ? "Sin empresa" : "ID " + dato.getValue().getEmpresaId()));
        personasTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> cargarSeleccion(seleccionada));
        crearButton.setVisible(permisos.contains("registrar_persona"));
        crearButton.setManaged(crearButton.isVisible());
        actualizarButton.setVisible(permisos.contains("actualizar_persona"));
        actualizarButton.setManaged(actualizarButton.isVisible());
        eliminarButton.setVisible(permisos.contains("eliminar_persona"));
        eliminarButton.setManaged(eliminarButton.isVisible());
        buscarButton.setDisable(!permisos.contains("consultar_persona"));
        empresaCombo.setDisable(!permisos.contains("gestionar_empresas"));
        if (permisos.contains("gestionar_empresas")) {
            empresaCombo.setItems(FXCollections.observableArrayList(
                    empresaService.consultarEmpresas(usuario.getUsername())));
        }
        if (permisos.contains("consultar_persona")) refrescarTabla();
    }

    @FXML
    private void crear() {
        ejecutar(() -> {
            Persona persona = personaService.registrarPersona(nombreField.getText().trim(),
                    documentoField.getText().trim(), tipoCombo.getValue(),
                    textoOpcional(fotoUrlField.getText()), usuario.getUsername());
            asociarEmpresaSeleccionada(persona);
            return persona;
        },
                "Persona registrada correctamente.");
    }

    @FXML
    private void actualizar() {
        if (documentoSeleccionado == null) {
            mostrarMensaje("Selecciona o busca una persona para actualizar.", true);
            return;
        }
        ejecutar(() -> {
            Persona persona = personaService.actualizarPersona(documentoSeleccionado,
                    nombreField.getText().trim(), documentoField.getText().trim(), tipoCombo.getValue(),
                    textoOpcional(fotoUrlField.getText()), usuario.getUsername());
            asociarEmpresaSeleccionada(persona);
            return persona;
        },
                "Persona actualizada correctamente.");
    }

    @FXML
    private void eliminar() {
        String documento = documentoSeleccionado != null
                ? documentoSeleccionado : documentoField.getText().trim();
        ejecutar(() -> { personaService.eliminarPersona(documento, usuario.getUsername()); return null; },
                "Persona eliminada correctamente.");
    }

    @FXML
    private void buscar() {
        try {
            Persona persona = personaService.consultarPersonaPorDocumento(
                    documentoField.getText().trim(), usuario.getUsername());
            cargarSeleccion(persona);
            personasTable.setItems(FXCollections.observableArrayList(persona));
            mostrarMensaje("Persona encontrada.", false);
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void limpiar() {
        documentoSeleccionado = null;
        nombreField.clear();
        documentoField.clear();
        fotoUrlField.clear();
        tipoCombo.setValue(null);
        empresaCombo.setValue(null);
        personasTable.getSelectionModel().clearSelection();
        if (permisos.contains("consultar_persona")) refrescarTabla();
    }

    @FXML
    private void volver() { navegador.mostrarPanel(usuario, nombreRol); }

    private void ejecutar(Operacion operacion, String mensajeExito) {
        try {
            operacion.ejecutar();
            mostrarMensaje(mensajeExito, false);
            limpiar();
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void refrescarTabla() {
        try {
            personasTable.setItems(FXCollections.observableArrayList(
                    personaService.consultarPersonas(usuario.getUsername())));
        } catch (RuntimeException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void cargarSeleccion(Persona persona) {
        if (persona == null) return;
        documentoSeleccionado = persona.getDocumento();
        nombreField.setText(persona.getNombre());
        documentoField.setText(persona.getDocumento());
        tipoCombo.setValue(persona.getTipo());
        fotoUrlField.setText(persona.getFotoUrl());
        empresaCombo.getItems().stream()
                .filter(empresa -> empresa.getId().equals(persona.getEmpresaId()))
                .findFirst().ifPresent(empresaCombo::setValue);
    }

    private void asociarEmpresaSeleccionada(Persona persona) {
        Empresa empresa = empresaCombo.getValue();
        if (empresa != null && permisos.contains("gestionar_empresas")) {
            personaService.asociarEmpresaAPersona(
                    persona.getId(), empresa.getId(), usuario.getUsername());
        }
    }

    private String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "No se pudo completar la operacion." : mensaje);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }

    @FunctionalInterface
    private interface Operacion { Object ejecutar(); }
}
