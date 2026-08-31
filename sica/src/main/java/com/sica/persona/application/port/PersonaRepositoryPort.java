package com.sica.persona.application.port;

import java.util.Optional;
import java.util.List;

import com.sica.persona.domain.Persona;
import com.sica.persona.domain.EstadoAcceso;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita del repositorio de personas, sin saber como esta implementado.
 */
public interface PersonaRepositoryPort {

    Persona guardar(Persona persona);

    void actualizar(Persona persona);

    void eliminar(Long id);

    boolean existePorDocumento(String documento);

    boolean existePorId(Long id);

    void asociarEmpresa(Long personaId, Long empresaId);

    void actualizarEstadoAcceso(Long personaId, EstadoAcceso estadoAcceso);

    Optional<Persona> buscarPorDocumento(String documento);

    Optional<Persona> buscarPorId(Long id);

    default List<Persona> listarTodos() {
        return List.of();
    }
}
