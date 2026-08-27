package com.sica.persona.application.port;

import java.util.Optional;

import com.sica.persona.domain.Persona;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita del repositorio de personas, sin saber como esta implementado.
 */
public interface PersonaRepositoryPort {

    Persona guardar(Persona persona);

    boolean existePorDocumento(String documento);

    boolean existePorId(Long id);

    void asociarEmpresa(Long personaId, Long empresaId);

    Optional<Persona> buscarPorDocumento(String documento);
}