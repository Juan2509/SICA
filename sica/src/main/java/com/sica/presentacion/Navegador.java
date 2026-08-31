package com.sica.presentacion;

import com.sica.autenticacion.application.LoginService;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.usuario.domain.Usuario;
import com.sica.persona.application.PersonaService;
import com.sica.empresa.application.EmpresaService;
import com.sica.visita.application.VisitaService;
import com.sica.incidente.application.IncidenteService;
import com.sica.auditoria.application.AuditoriaService;
import com.sica.reporte.application.ReporteService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set;

/** Cambia las pantallas sin mezclar esta tarea con la logica de negocio. */
public class Navegador {

    private final Stage stage;
    private final LoginService loginService;
    private final RolRepositoryPort rolRepository;
    private final PersonaService personaService;
    private final EmpresaService empresaService;
    private final VisitaService visitaService;
    private final IncidenteService incidenteService;
    private final AuditoriaService auditoriaService;
    private final ReporteService reporteService;

    public Navegador(Stage stage, LoginService loginService,
                     RolRepositoryPort rolRepository, PersonaService personaService,
                     EmpresaService empresaService, VisitaService visitaService,
                     IncidenteService incidenteService, AuditoriaService auditoriaService,
                     ReporteService reporteService) {
        this.stage = stage;
        this.loginService = loginService;
        this.rolRepository = rolRepository;
        this.personaService = personaService;
        this.empresaService = empresaService;
        this.visitaService = visitaService;
        this.incidenteService = incidenteService;
        this.auditoriaService = auditoriaService;
        this.reporteService = reporteService;
    }

    public void mostrarLogin() {
        LoginController controller = new LoginController(
                loginService, rolRepository, this);
        mostrarPantalla("login.fxml", controller, "SICA | Inicio de sesión", 940, 600);
    }

    public void mostrarConfiguracionBD() {
        ConfiguracionBDController controller = new ConfiguracionBDController(this);
        mostrarPantalla("configuracion-bd.fxml", controller,
                "SICA | Configuracion de PostgreSQL", 720, 570);
    }

    public void mostrarPanel(Usuario usuario, String nombreRol) {
        PanelPrincipalController controller = new PanelPrincipalController(
                usuario, nombreRol, rolRepository, this);
        mostrarPantalla("panel-principal.fxml", controller,
                "SICA | Panel principal", 1100, 680);
    }

    public void mostrarPersonas(Usuario usuario, String nombreRol) {
        PersonasController controller = new PersonasController(usuario, nombreRol,
                obtenerPermisos(usuario), personaService, empresaService, this);
        mostrarPantalla("personas.fxml", controller,
                "SICA | Gestion de personas", 1180, 720);
    }

    public void mostrarEmpresas(Usuario usuario, String nombreRol) {
        EmpresasController controller = new EmpresasController(
                usuario, nombreRol, empresaService, this);
        mostrarPantalla("empresas.fxml", controller,
                "SICA | Gestion de empresas", 1100, 680);
    }

    public void mostrarVisitas(Usuario usuario, String nombreRol) {
        VisitasController controller = new VisitasController(usuario, nombreRol,
                obtenerPermisos(usuario), visitaService, this);
        mostrarPantalla("visitas.fxml", controller,
                "SICA | Gestion de visitas", 1200, 750);
    }

    public void mostrarSolicitudes(Usuario usuario, String nombreRol) {
        SolicitudesController controller = new SolicitudesController(usuario, nombreRol,
                obtenerPermisos(usuario), visitaService, this);
        mostrarPantalla("solicitudes.fxml", controller,
                "SICA | Solicitudes de aprobacion", 1200, 740);
    }

    public void mostrarIncidentes(Usuario usuario, String nombreRol) {
        IncidentesController controller = new IncidentesController(
                usuario, nombreRol, incidenteService, this);
        mostrarPantalla("incidentes.fxml", controller,
                "SICA | Registro de incidentes", 900, 620);
    }

    public void mostrarAuditoria(Usuario usuario, String nombreRol) {
        AuditoriaController controller = new AuditoriaController(
                usuario, nombreRol, auditoriaService, this);
        mostrarPantalla("auditoria.fxml", controller,
                "SICA | Bitacora de auditoria", 1250, 720);
    }

    public void mostrarReportes(Usuario usuario, String nombreRol) {
        ReportesController controller = new ReportesController(
                usuario, nombreRol, reporteService, this);
        mostrarPantalla("reportes.fxml", controller,
                "SICA | Reportes", 1200, 720);
    }

    private Set<String> obtenerPermisos(Usuario usuario) {
        return rolRepository.listarPermisosDeRol(usuario.getRolId()).stream()
                .map(com.sica.rol.domain.Permiso::getNombre)
                .collect(java.util.stream.Collectors.toSet());
    }

    private void mostrarPantalla(String archivoFxml, Object controller,
                                  String titulo, double ancho, double alto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SicaApplication.class.getResource(archivoFxml));
            loader.setController(controller);
            Parent raiz = loader.load();
            Scene escena = new Scene(raiz, ancho, alto);
            escena.getStylesheets().add(
                    SicaApplication.class.getResource("estilos.css").toExternalForm());

            stage.setTitle(titulo);
            stage.setMinWidth(820);
            stage.setMinHeight(540);
            stage.setScene(escena);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("No se pudo cargar la interfaz: " + archivoFxml, e);
        }
    }
}
