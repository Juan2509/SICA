package com.sica.presentacion;

import com.sica.auditoria.infrastructure.BitacoraAuditoriaJdbcAdapter;
import com.sica.autenticacion.application.LoginService;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.infrastructure.RolRepositoryJdbcAdapter;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.infrastructure.UsuarioRepositoryJdbcAdapter;
import com.sica.autorizacion.application.AutorizacionService;
import com.sica.empresa.application.EmpresaService;
import com.sica.empresa.application.port.EmpresaRepositoryPort;
import com.sica.empresa.infrastructure.EmpresaRepositoryJdbcAdapter;
import com.sica.persona.application.PersonaService;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.infrastructure.PersonaRepositoryJdbcAdapter;
import com.sica.visita.application.VisitaService;
import com.sica.visita.infrastructure.VisitaRepositoryJdbcAdapter;
import com.sica.visita.application.port.VisitaRepositoryPort;
import com.sica.incidente.application.IncidenteService;
import com.sica.incidente.infrastructure.IncidenteRepositoryJdbcAdapter;
import com.sica.auditoria.application.AuditoriaService;
import com.sica.reporte.application.ReporteService;
import com.sica.infraestructura.ConexionBD;
import javafx.application.Application;
import javafx.stage.Stage;

/** Punto de arranque y composicion de la interfaz JavaFX. */
public class SicaApplication extends Application {

    @Override
    public void start(Stage stage) {
        UsuarioRepositoryPort usuarios = new UsuarioRepositoryJdbcAdapter();
        RolRepositoryPort roles = new RolRepositoryJdbcAdapter();
        BitacoraAuditoriaJdbcAdapter bitacora = new BitacoraAuditoriaJdbcAdapter();
        LoginService loginService = new LoginService(usuarios, bitacora);
        AutorizacionService autorizacion = new AutorizacionService(usuarios, roles);
        EmpresaRepositoryPort empresas = new EmpresaRepositoryJdbcAdapter();
        PersonaRepositoryPort personas = new PersonaRepositoryJdbcAdapter();
        EmpresaService empresaService = new EmpresaService(empresas, bitacora, autorizacion);
        PersonaService personaService = new PersonaService(personas, empresas, bitacora, autorizacion);
        VisitaRepositoryPort visitas = new VisitaRepositoryJdbcAdapter();
        VisitaService visitaService = new VisitaService(visitas, personas, bitacora, autorizacion);
        IncidenteService incidenteService = new IncidenteService(
                new IncidenteRepositoryJdbcAdapter(), personas, bitacora, autorizacion);
        AuditoriaService auditoriaService = new AuditoriaService(bitacora, autorizacion);
        ReporteService reporteService = new ReporteService(visitas, personas, bitacora, autorizacion);

        Navegador navegador = new Navegador(stage, loginService, roles,
                personaService, empresaService, visitaService, incidenteService,
                auditoriaService, reporteService);
        if (ConexionBD.faltaConfiguracion()) navegador.mostrarConfiguracionBD();
        else navegador.mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
