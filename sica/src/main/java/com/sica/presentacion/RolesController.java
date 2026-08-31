package com.sica.presentacion;

import com.sica.rol.application.RolService;
import com.sica.rol.domain.Permiso;
import com.sica.rol.domain.Rol;
import com.sica.usuario.domain.Usuario;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/** Permite consultar roles y asociarles permisos almacenados en PostgreSQL. */
public class RolesController {
    private final Usuario usuario;
    private final String nombreRol;
    private final RolService rolService;
    private final Navegador navegador;
    @FXML private ListView<Rol> rolesList;
    @FXML private ListView<Permiso> permisosRolList;
    @FXML private ComboBox<Permiso> permisoCombo;
    @FXML private Label mensajeLabel;

    public RolesController(Usuario usuario, String nombreRol, RolService rolService,
                           Navegador navegador) {
        this.usuario = usuario; this.nombreRol = nombreRol;
        this.rolService = rolService; this.navegador = navegador;
    }

    @FXML private void initialize() {
        rolesList.setCellFactory(v -> celdaRol());
        permisosRolList.setCellFactory(v -> celdaPermiso());
        permisoCombo.setCellFactory(v -> celdaPermiso());
        permisoCombo.setButtonCell(celdaPermiso());
        rolesList.setItems(FXCollections.observableArrayList(rolService.consultarRoles()));
        permisoCombo.setItems(FXCollections.observableArrayList(rolService.consultarPermisos()));
        rolesList.getSelectionModel().selectedItemProperty().addListener(
                (o, anterior, rol) -> cargarPermisos(rol));
        if (!rolesList.getItems().isEmpty()) rolesList.getSelectionModel().selectFirst();
    }

    private ListCell<Rol> celdaRol() { return new ListCell<>() {
        @Override protected void updateItem(Rol item, boolean empty) {
            super.updateItem(item, empty); setText(empty || item == null ? null : item.getNombre());
        }}; }
    private ListCell<Permiso> celdaPermiso() { return new ListCell<>() {
        @Override protected void updateItem(Permiso item, boolean empty) {
            super.updateItem(item, empty); setText(empty || item == null ? null : item.getNombre());
        }}; }

    private void cargarPermisos(Rol rol) {
        permisosRolList.setItems(rol == null ? FXCollections.observableArrayList()
                : FXCollections.observableArrayList(rolService.consultarPermisosDeRol(rol.getId())));
    }

    @FXML private void asociar() {
        Rol rol = rolesList.getSelectionModel().getSelectedItem();
        Permiso permiso = permisoCombo.getValue();
        if (rol == null || permiso == null) { mostrar("Selecciona un rol y un permiso.", true); return; }
        try {
            rolService.asociarPermisoARol(rol.getId(), permiso.getId(), usuario.getUsername());
            cargarPermisos(rol); mostrar("Permiso asociado correctamente.", false);
        } catch (RuntimeException e) { mostrar(e.getMessage(), true); }
    }

    @FXML private void volver() { navegador.mostrarPanel(usuario, nombreRol); }
    private void mostrar(String texto, boolean error) {
        mensajeLabel.setText(texto == null ? "No se pudo completar la operacion." : texto);
        mensajeLabel.getStyleClass().setAll(error ? "screen-error" : "screen-success");
    }
}
