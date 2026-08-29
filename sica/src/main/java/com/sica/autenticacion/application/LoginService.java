package com.sica.autenticacion.application;

import java.util.Optional;

import com.sica.autenticacion.application.exception.CredencialesInvalidasException;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;

/**
 * Servicio de aplicacion para la Historia de Usuario E1-HU04 (Login).
 * Tanto los inicios de sesion exitosos como los fallidos quedan en la bitacora.
 */
public class LoginService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;

    public LoginService(UsuarioRepositoryPort usuarioRepository, BitacoraAuditoriaPort bitacoraAuditoria) {
        this.usuarioRepository = usuarioRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
    }

    /**
     * Intenta iniciar sesion con las credenciales dadas.
     *
     * @param username usuario que intenta ingresar
     * @param password contrasena proporcionada
     * @return el usuario autenticado, si las credenciales son correctas
     */
    public Usuario iniciarSesion(String username, String password) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorUsername(username);

        boolean credencialesValidas = usuarioEncontrado.isPresent()
                && usuarioEncontrado.get().getPassword().equals(password);

        if (!credencialesValidas) {
            bitacoraAuditoria.registrar(
                    "LOGIN_FALLIDO",
                    "Intento fallido de inicio de sesion para el username: " + username,
                    username
            );
            throw new CredencialesInvalidasException("Usuario o contrasena incorrectos.");
        }

        bitacoraAuditoria.registrar(
                "LOGIN_EXITOSO",
                "Inicio de sesion exitoso",
                username
        );

        return usuarioEncontrado.get();
    }
}
