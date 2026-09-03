package com.sica.presentacion;

import com.sica.rol.application.RolService;
import com.sica.rol.domain.Rol;
import com.sica.usuario.application.UsuarioService;
import com.sica.usuario.domain.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Set;

/** Pantalla sencilla para administrar los usuarios de SICA. */
public class UsuariosController {
    private final Usuario usuarioActual;
    private final String nombreRol;
    private final Set<String> permisos;
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final Navegador navegador;

    @FXML private TextField nombreField;
    @FXML private TextField documentoField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Rol> rolCombo;
    @FXML private Button guardarButton;
    @FXML private Button actualizarButton;
    @FXML private Button eliminarButton;
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, String> nombreColumn;
    @FXML private TableColumn<Usuario, String> documentoColumn;
    @FXML private TableColumn<Usuario, String> usernameColumn;
    @FXML private TableColumn<Usuario, String> rolColumn;
    @FXML private Label mensajeLabel;

    public UsuariosController(Usuario usuarioActual, String nombreRol, Set<String> permisos,
                              UsuarioService usuarioService, RolService rolService,
                              Navegador navegador) {
        this.usuarioActual = usuarioActual;
        this.nombreRol = nombreRol;
        this.permisos = permisos;
        this.usuarioService = usuarioService;
        this.rolService = rolService;
        this.navegador = navegador;
    }

    @FXML private void initialize() {
        usuariosTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        rolCombo.setItems(FXCollections.observableArrayList(rolService.consultarRoles()));
        rolCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Rol rol) { return rol == null ? "" : rol.getNombre(); }
            @Override public Rol fromString(String texto) { return null; }
        });
        nombreColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getNombre()));
        documentoColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getDocumento()));
        usernameColumn.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getUsername()));
        rolColumn.setCellValueFactory(f -> new SimpleStringProperty(nombreDelRol(f.getValue().getRolId())));
        guardarButton.setDisable(!permisos.contains("crear_usuario"));
        actualizarButton.setDisable(!permisos.contains("actualizar_usuario"));
        eliminarButton.setDisable(!permisos.contains("eliminar_usuario"));
        usuariosTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> {
                    cargarFormulario(seleccionado);
                    actualizarEstadoEliminar(seleccionado);
                });
        refrescar();
    }

    @FXML private void guardar() {
        ejecutar(() -> usuarioService.crearUsuario(nombreField.getText(), documentoField.getText(),
                usernameField.getText(), passwordField.getText(), rolSeleccionado(),
                usuarioActual.getUsername()), "Usuario creado correctamente.");
    }

    @FXML private void actualizar() {
        Usuario seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrar("Selecciona el usuario que deseas actualizar.", true); return; }
        ejecutar(() -> {
            Usuario actualizado = usuarioService.actualizarUsuario(seleccionado.getUsername(),
                    nombreField.getText(), documentoField.getText(), usernameField.getText(),
                    passwordField.getText(), rolSeleccionado(), usuarioActual.getUsername());
            if (seleccionado.getId().equals(usuarioActual.getId())) {
                usuarioActual.actualizarSesion(actualizado);
            }
            return actualizado;
        }, "Usuario actualizado correctamente.");
    }

    @FXML private void eliminar() {
        Usuario seleccionado = usuariosTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrar("Selecciona el usuario que deseas eliminar.", true); return; }
        ejecutar(() -> { usuarioService.eliminarUsuario(seleccionado.getUsername(),
                usuarioActual.getUsername()); return null; }, "Usuario eliminado correctamente.");
    }

    private void ejecutar(java.util.concurrent.Callable<?> accion, String exito) {
        try { accion.call(); refrescar(); limpiar(); mostrar(exito, false); }
        catch (Exception e) { mostrar(e.getMessage(), true); }
    }

    private Long rolSeleccionado() {
        return rolCombo.getValue() == null ? null : rolCombo.getValue().getId();
    }

    private void refrescar() {
        usuariosTable.setItems(FXCollections.observableArrayList(usuarioService.consultarUsuarios()));
    }

    private String nombreDelRol(Long id) {
        return rolCombo.getItems().stream().filter(r -> r.getId().equals(id))
                .map(Rol::getNombre).findFirst().orElse("Rol " + id);
    }

    private void cargarFormulario(Usuario usuario) {
        if (usuario == null) return;
        nombreField.setText(usuario.getNombre()); documentoField.setText(usuario.getDocumento());
        usernameField.setText(usuario.getUsername()); passwordField.setText(usuario.getPassword());
        rolCombo.getItems().stream().filter(r -> r.getId().equals(usuario.getRolId()))
                .findFirst().ifPresent(rolCombo::setValue);
    }

    private void actualizarEstadoEliminar(Usuario seleccionado) {
        boolean sinPermiso = !permisos.contains("eliminar_usuario");
        boolean cuentaActual = seleccionado != null
                && seleccionado.getUsername().equals(usuarioActual.getUsername());
        boolean administradorPrincipal = seleccionado != null
                && seleccionado.isAdministradorPrincipal();
        eliminarButton.setDisable(sinPermiso || cuentaActual || administradorPrincipal);
    }

    @FXML private void limpiar() {
        usuariosTable.getSelectionModel().clearSelection(); nombreField.clear(); documentoField.clear();
        usernameField.clear(); passwordField.clear(); rolCombo.setValue(null);
        actualizarEstadoEliminar(null);
    }

    @FXML private void volver() { navegador.mostrarPanel(usuarioActual, nombreRol); }
    private void mostrar(String texto, boolean error) {
        mensajeLabel.setText(texto == null ? "No se pudo completar la operacion." : texto);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
