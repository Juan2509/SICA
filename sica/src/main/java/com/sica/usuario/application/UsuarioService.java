package com.sica.usuario.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.usuario.application.exception.UsuarioDuplicadoException;
import com.sica.usuario.application.exception.UsuarioInvalidoException;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;
import java.util.List;

/**
 * Servicio de aplicacion para la Historia de Usuario E1-HU01 (Crear usuario).
 * Contiene la logica de negocio, independiente de como se guarden los datos.
 */
public class UsuarioService {

    private static final String PERMISO_CREAR_USUARIO = "crear_usuario";
    private static final String PERMISO_ACTUALIZAR_USUARIO = "actualizar_usuario";
    private static final String PERMISO_ELIMINAR_USUARIO = "eliminar_usuario";

    private final UsuarioRepositoryPort usuarioRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository, BitacoraAuditoriaPort bitacoraAuditoria,
                           AutorizacionService autorizacionService) {
        this.usuarioRepository = usuarioRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * @param nombre              nombre completo del usuario (obligatorio)
     * @param documento           documento de identidad (obligatorio)
     * @param username            identificador unico de acceso (obligatorio, no se puede repetir)
     * @param password            contrasena del usuario (obligatorio)
     * @param rolId               id del rol asociado (obligatorio)
     * @param usuarioResponsable  username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     * @return el usuario creado, con su id ya asignado
     */
    public Usuario crearUsuario(String nombre, String documento, String username,
                                 String password, Long rolId, String usuarioResponsable) {

        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CREAR_USUARIO);

        validarDatosObligatorios(nombre, documento, username, password, rolId);

        if (usuarioRepository.existePorUsername(username)) {
            throw new UsuarioDuplicadoException("Ya existe un usuario con el username: " + username);
        }

        Usuario nuevoUsuario = new Usuario(nombre, documento, username, password, rolId);
        Usuario usuarioGuardado = usuarioRepository.guardar(nuevoUsuario);

        bitacoraAuditoria.registrar(
                "CREAR_USUARIO",
                "Se creo el usuario con username: " + username,
                usuarioResponsable
        );

        return usuarioGuardado;
    }

    public Usuario actualizarUsuario(String usernameActual, String nombre, String documento,
                                      String nuevoUsername, String password, Long rolId,
                                      String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_ACTUALIZAR_USUARIO);
        validarDatosObligatorios(nombre, documento, nuevoUsername, password, rolId);

        Usuario usuarioActual = usuarioRepository.buscarPorUsername(usernameActual)
                .orElseThrow(() -> new UsuarioInvalidoException(
                        "No existe un usuario con el username: " + usernameActual));

        if (!usernameActual.equals(nuevoUsername)
                && usuarioRepository.existePorUsername(nuevoUsername)) {
            throw new UsuarioDuplicadoException(
                    "Ya existe un usuario con el username: " + nuevoUsername);
        }

        Usuario usuarioActualizado = new Usuario(nombre, documento, nuevoUsername, password, rolId);
        usuarioActualizado.setId(usuarioActual.getId());
        usuarioActualizado.setAdministradorPrincipal(usuarioActual.isAdministradorPrincipal());
        usuarioRepository.actualizar(usuarioActualizado);

        bitacoraAuditoria.registrar(
                "ACTUALIZAR_USUARIO",
                "Se actualizo el usuario con id: " + usuarioActual.getId(),
                usuarioResponsable
        );
        return usuarioActualizado;
    }

    public void eliminarUsuario(String username, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_ELIMINAR_USUARIO);

        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new UsuarioInvalidoException(
                        "No existe un usuario con el username: " + username));

        if (username.equals(usuarioResponsable)) {
            throw new UsuarioInvalidoException(
                    "No puedes eliminar el usuario con el que tienes la sesión iniciada.");
        }
        if (usuario.isAdministradorPrincipal()) {
            throw new UsuarioInvalidoException(
                    "El administrador principal de SICA no puede eliminarse.");
        }

        usuarioRepository.eliminar(usuario.getId());
        bitacoraAuditoria.registrar(
                "ELIMINAR_USUARIO",
                "Se elimino el usuario con id: " + usuario.getId() + " y username: " + username,
                usuarioResponsable
        );
    }

    public List<Usuario> consultarUsuarios() {
        return usuarioRepository.listarTodos();
    }

    private void validarDatosObligatorios(String nombre, String documento, String username,
                                           String password, Long rolId) {
        if (esVacio(nombre)) {
            throw new UsuarioInvalidoException("El nombre es obligatorio.");
        }
        if (esVacio(documento)) {
            throw new UsuarioInvalidoException("El documento es obligatorio.");
        }
        if (esVacio(username)) {
            throw new UsuarioInvalidoException("El username es obligatorio.");
        }
        if (esVacio(password)) {
            throw new UsuarioInvalidoException("La contrasena es obligatoria.");
        }
        if (rolId == null) {
            throw new UsuarioInvalidoException("El usuario debe estar asociado a un rol.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
