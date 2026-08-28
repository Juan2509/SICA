package com.sica.autorizacion.application;

import com.sica.autorizacion.application.exception.AccesoDenegadoException;
import com.sica.rol.application.port.RolRepositoryPort;
import com.sica.usuario.application.port.UsuarioRepositoryPort;
import com.sica.usuario.domain.Usuario;

import java.util.Optional;

/**
 * Servicio de aplicacion para la Historia de Usuario E1-HU03 (Validar permisos).
 * Antes de ejecutar una operacion critica, cualquier servicio debe llamar a
 * verificarPermiso(...). El permiso nunca se compara por texto en un "if de rol",
 * siempre se consulta en base de datos a traves de RolRepositoryPort.
 */
public class AutorizacionService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RolRepositoryPort rolRepository;

    public AutorizacionService(UsuarioRepositoryPort usuarioRepository, RolRepositoryPort rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    /**
     * Verifica que el usuario tenga el permiso indicado.
     * Si no lo tiene (o el usuario no existe), lanza AccesoDenegadoException
     * con un mensaje claro y la operacion no continua.
     *
     * @param username      usuario que intenta ejecutar la operacion
     * @param codigoPermiso permiso requerido (ej. "crear_usuario")
     */
    public void verificarPermiso(String username, String codigoPermiso) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorUsername(username);

        if (usuarioEncontrado.isEmpty()) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: no existe el usuario '" + username + "'."
            );
        }

        Usuario usuario = usuarioEncontrado.get();
        boolean tienePermiso = rolRepository.tienePermiso(usuario.getRolId(), codigoPermiso);

        if (!tienePermiso) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: el usuario '" + username
                            + "' no tiene permiso para realizar esta accion ('" + codigoPermiso + "')."
            );
        }
    }

    /**
     * Verifica que el usuario corresponda a la persona visitada.
     * Ambos registros se relacionan mediante su numero de documento.
     */
    public void verificarUsuarioCorrespondeAPersona(String username, String documentoPersona) {
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .orElseThrow(() -> new AccesoDenegadoException(
                        "Acceso denegado: no existe el usuario '" + username + "'."));

        if (!usuario.getDocumento().equals(documentoPersona)) {
            throw new AccesoDenegadoException(
                    "Acceso denegado: la solicitud no corresponde al funcionario '" + username + "'.");
        }
    }
}
