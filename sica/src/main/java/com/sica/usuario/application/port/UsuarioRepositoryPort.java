package com.sica.usuario.application.port;

import java.util.Optional;

import com.sica.usuario.domain.Usuario;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita del repositorio de usuarios, sin saber como esta implementado.
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    void actualizar(Usuario usuario);

    void eliminar(Long id);

    boolean existePorId(Long id);

    boolean existePorUsername(String username);

    Optional<Usuario> buscarPorUsername(String username);
}
