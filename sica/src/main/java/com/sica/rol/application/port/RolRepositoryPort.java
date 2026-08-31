package com.sica.rol.application.port;

import com.sica.rol.domain.Permiso;
import com.sica.rol.domain.Rol;

import java.util.List;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita para consultar roles y administrar sus permisos,
 * sin saber como esta implementado (base de datos, etc).
 */
public interface RolRepositoryPort {

    List<Rol> listarRoles();

    boolean existeRolPorId(Long rolId);

    boolean existePermisoPorId(Long permisoId);

    void asociarPermiso(Long rolId, Long permisoId);

    List<Permiso> listarPermisosDeRol(Long rolId);

    List<Permiso> listarPermisos();

    boolean tienePermiso(Long rolId, String codigoPermiso);
}
