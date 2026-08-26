package com.sica.rol.application;

import java.util.List;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.rol.application.exception.PermisoNoEncontradoException;
import com.sica.rol.application.exception.RolNoEncontradoException;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.rol.domain.Permiso;
import com.sica.rol.domain.Rol;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;

/**
 * Servicio de aplicacion para la Historia de Usuario E1-HU02 (Gestionar roles).
 * Los permisos siempre se consultan en base de datos a traves de RolRepositoryPort,
 * nunca se escriben condiciones de permisos directamente aqui.
 */
public class RolService {

    private static final String PERMISO_ADMINISTRAR_ROLES = "administrar_roles";

    private final RolRepositoryPort rolRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public RolService(RolRepositoryPort rolRepository, BitacoraAuditoriaPort bitacoraAuditoria,
                       AutorizacionService autorizacionService) {
        this.rolRepository = rolRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Consulta todos los roles existentes en el sistema.
     */
    public List<Rol> consultarRoles() {
        return rolRepository.listarRoles();
    }

    /**
     * Consulta los permisos asociados a un rol especifico.
     */
    public List<Permiso> consultarPermisosDeRol(Long rolId) {
        if (!rolRepository.existeRolPorId(rolId)) {
            throw new RolNoEncontradoException("No existe un rol con id: " + rolId);
        }
        return rolRepository.listarPermisosDeRol(rolId);
    }

    /**
     * Asocia un permiso existente a un rol existente.
     *
     * @param rolId              id del rol
     * @param permisoId          id del permiso a asociar
     * @param usuarioResponsable username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     */
    public void asociarPermisoARol(Long rolId, Long permisoId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_ADMINISTRAR_ROLES);

        if (!rolRepository.existeRolPorId(rolId)) {
            throw new RolNoEncontradoException("No existe un rol con id: " + rolId);
        }
        if (!rolRepository.existePermisoPorId(permisoId)) {
            throw new PermisoNoEncontradoException("No existe un permiso con id: " + permisoId);
        }

        rolRepository.asociarPermiso(rolId, permisoId);

        bitacoraAuditoria.registrar(
                "ASOCIAR_PERMISO_ROL",
                "Se asocio el permiso " + permisoId + " al rol " + rolId,
                usuarioResponsable
        );
    }

    /**
     * Verifica si un rol tiene un permiso especifico, consultando la base de datos.
     */
    public boolean rolTienePermiso(Long rolId, String codigoPermiso) {
        return rolRepository.tienePermiso(rolId, codigoPermiso);
    }
}