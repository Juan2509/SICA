package com.sica.integracion;

import com.sica.auditoria.application.AuditoriaService;
import com.sica.auditoria.infrastructure.BitacoraAuditoriaJdbcAdapter;
import com.sica.autenticacion.application.LoginService;
import com.sica.autenticacion.application.exception.CredencialesInvalidasException;
import com.sica.autorizacion.application.AutorizacionService;
import com.sica.autorizacion.application.exception.AccesoDenegadoException;
import com.sica.infraestructura.ConexionBD;
import com.sica.persona.infrastructure.PersonaRepositoryJdbcAdapter;
import com.sica.rol.infrastructure.RolRepositoryJdbcAdapter;
import com.sica.usuario.domain.Usuario;
import com.sica.usuario.infrastructure.UsuarioRepositoryJdbcAdapter;
import com.sica.visita.application.VisitaService;
import com.sica.visita.application.dto.DetalleVisitaConsulta;
import com.sica.visita.domain.EstadoVisita;
import com.sica.visita.infrastructure.VisitaRepositoryJdbcAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Prueba opcional contra la instalacion PostgreSQL real de SICA. */
class PostgresqlIntegracionTest {

    private static UsuarioRepositoryJdbcAdapter usuarios;
    private static RolRepositoryJdbcAdapter roles;
    private static BitacoraAuditoriaJdbcAdapter bitacora;
    private static AutorizacionService autorizacion;

    @BeforeAll
    static void prepararAdaptadores() {
        String password = System.getenv("SICA_DB_PASSWORD");
        assumeTrue(password != null && !password.isBlank(),
                "Define SICA_DB_PASSWORD para ejecutar la integracion PostgreSQL.");

        usuarios = new UsuarioRepositoryJdbcAdapter();
        roles = new RolRepositoryJdbcAdapter();
        bitacora = new BitacoraAuditoriaJdbcAdapter();
        autorizacion = new AutorizacionService(usuarios, roles);
    }

    @Test
    void conectaConLaBaseYElUsuarioTecnico() throws Exception {
        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement statement = conexion.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "SELECT current_database(), current_user")) {
            assertTrue(resultado.next());
            assertEquals("sica_db", resultado.getString(1));
            assertEquals("sica_app", resultado.getString(2));
        }
    }

    @Test
    void mantieneLoginRbacAuditoriaYConsultaDeVisita() {
        LoginService loginService = new LoginService(usuarios, bitacora);

        Usuario administrador = loginService.iniciarSesion("admin", "admin123");
        assertEquals("Administrador SICA", administrador.getNombre());

        assertThrows(CredencialesInvalidasException.class,
                () -> loginService.iniciarSesion("admin", "clave-incorrecta"));

        autorizacion.verificarPermiso("admin", "crear_usuario");
        assertThrows(AccesoDenegadoException.class,
                () -> autorizacion.verificarPermiso("guarda", "crear_usuario"));

        VisitaService visitaService = new VisitaService(
                new VisitaRepositoryJdbcAdapter(),
                new PersonaRepositoryJdbcAdapter(),
                bitacora,
                autorizacion
        );
        DetalleVisitaConsulta visita = visitaService.consultarVisitaPorDocumento(
                "30000001", "guarda");
        assertEquals(EstadoVisita.APROBADO, visita.getEstado());

        AuditoriaService auditoriaService = new AuditoriaService(bitacora, autorizacion);
        assertTrue(auditoriaService.consultarBitacora("admin").stream()
                .anyMatch(registro -> registro.getAccion().equals("LOGIN_EXITOSO")));
        assertTrue(auditoriaService.consultarBitacora("admin").stream()
                .anyMatch(registro -> registro.getAccion().equals("LOGIN_FALLIDO")));
    }
}
