package com.sica.presentacion;

import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.domain.Permiso;
import com.sica.usuario.domain.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Panel principal cuyo menu se construye con los permisos del rol autenticado. */
public class PanelPrincipalController {
    private final Usuario usuario;
    private final String nombreRol;
    private final RolRepositoryPort rolRepository;
    private final Navegador navegador;

    @FXML private Label nombreUsuarioLabel;
    @FXML private Label rolUsuarioLabel;
    @FXML private Label inicialesLabel;
    @FXML private Label tituloContenidoLabel;
    @FXML private Label descripcionContenidoLabel;
    @FXML private Label cantidadModulosLabel;
    @FXML private Button usuariosButton;
    @FXML private Button rolesButton;
    @FXML private Button personasButton;
    @FXML private Button empresasButton;
    @FXML private Button visitasButton;
    @FXML private Button accesoButton;
    @FXML private Button solicitudesButton;
    @FXML private Button incidentesButton;
    @FXML private Button auditoriaButton;
    @FXML private Button reportesButton;

    public PanelPrincipalController(Usuario usuario, String nombreRol,
                                    RolRepositoryPort rolRepository,
                                    Navegador navegador) {
        this.usuario = usuario;
        this.nombreRol = nombreRol;
        this.rolRepository = rolRepository;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        nombreUsuarioLabel.setText(usuario.getNombre());
        rolUsuarioLabel.setText(nombreRol);
        inicialesLabel.setText(obtenerIniciales(usuario.getNombre()));
        configurarMenuSegunPermisos();
    }

    private void configurarMenuSegunPermisos() {
        Set<String> permisos = rolRepository.listarPermisosDeRol(usuario.getRolId()).stream()
                .map(Permiso::getNombre)
                .collect(Collectors.toSet());

        configurarOpcion(usuariosButton, tieneAlguno(permisos,
                "crear_usuario", "actualizar_usuario", "eliminar_usuario"));
        configurarOpcion(rolesButton, permisos.contains("administrar_roles"));
        configurarOpcion(personasButton, tieneAlguno(permisos,
                "registrar_persona", "actualizar_persona", "eliminar_persona",
                "consultar_persona", "bloquear_persona"));
        configurarOpcion(empresasButton, permisos.contains("gestionar_empresas"));
        configurarOpcion(visitasButton, tieneAlguno(permisos,
                "registrar_visita", "consultar_visita"));
        configurarOpcion(accesoButton, tieneAlguno(permisos,
                "registrar_checkin", "registrar_checkout",
                "registrar_visitante_no_anunciado", "solicitar_ingreso_por_olvido"));
        configurarOpcion(solicitudesButton, tieneAlguno(permisos,
                "responder_solicitud_visita", "registrar_visitante_no_anunciado",
                "solicitar_ingreso_por_olvido"));
        configurarOpcion(incidentesButton, permisos.contains("registrar_incidente"));
        configurarOpcion(auditoriaButton, permisos.contains("consultar_bitacora"));
        configurarOpcion(reportesButton, permisos.contains("generar_reporte"));

        long cantidad = botonesDelMenu().stream().filter(Button::isVisible).count();
        cantidadModulosLabel.setText(cantidad + " modulos disponibles para tu rol");
    }

    private List<Button> botonesDelMenu() {
        return List.of(usuariosButton, rolesButton, personasButton, empresasButton,
                visitasButton, accesoButton, solicitudesButton, incidentesButton,
                auditoriaButton, reportesButton);
    }

    private boolean tieneAlguno(Set<String> permisos, String... requeridos) {
        for (String requerido : requeridos) {
            if (permisos.contains(requerido)) return true;
        }
        return false;
    }

    private void configurarOpcion(Button boton, boolean permitido) {
        boton.setVisible(permitido);
        boton.setManaged(permitido);
    }

    @FXML
    private void mostrarInicio() {
        mostrarModulo("Panel principal", "Selecciona una opcion del menu lateral para comenzar.");
    }

    @FXML
    private void seleccionarModulo(ActionEvent evento) {
        Button opcion = (Button) evento.getSource();
        if (opcion == personasButton) {
            navegador.mostrarPersonas(usuario, nombreRol);
            return;
        }
        if (opcion == empresasButton) {
            navegador.mostrarEmpresas(usuario, nombreRol);
            return;
        }
        if (opcion == visitasButton) {
            navegador.mostrarVisitas(usuario, nombreRol);
            return;
        }
        if (opcion == solicitudesButton) {
            navegador.mostrarSolicitudes(usuario, nombreRol);
            return;
        }
        if (opcion == incidentesButton) {
            navegador.mostrarIncidentes(usuario, nombreRol);
            return;
        }
        if (opcion == auditoriaButton) {
            navegador.mostrarAuditoria(usuario, nombreRol);
            return;
        }
        if (opcion == reportesButton) {
            navegador.mostrarReportes(usuario, nombreRol);
            return;
        }
        mostrarModulo(opcion.getText(), "Tu rol tiene permiso para acceder a este modulo. "
                + "Su pantalla funcional se implementara en la siguiente etapa.");
    }

    private void mostrarModulo(String titulo, String descripcion) {
        tituloContenidoLabel.setText(titulo);
        descripcionContenidoLabel.setText(descripcion);
    }

    @FXML
    private void cerrarSesion() {
        navegador.mostrarLogin();
    }

    private String obtenerIniciales(String nombre) {
        StringBuilder iniciales = new StringBuilder();
        for (String parte : nombre.trim().split("\\s+")) {
            if (!parte.isEmpty() && iniciales.length() < 2) {
                iniciales.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return iniciales.toString();
    }
}
