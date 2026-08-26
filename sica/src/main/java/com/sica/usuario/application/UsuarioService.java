package com.sica.usuario.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.usuario.application.exception.UsuarioDuplicadoException;
import com.sica.usuario.application.exception.UsuarioInvalidoException;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;

/**
 * Servicio de aplicacion para la Historia de Usuario E1-HU01 (Crear usuario).
 * Contiene la logica de negocio, independiente de como se guarden los datos.
 */
public class UsuarioService {

    private static final String PERMISO_CREAR_USUARIO = "crear_usuario";

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