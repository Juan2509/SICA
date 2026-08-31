package com.sica.empresa.application.port;

import com.sica.empresa.domain.Empresa;
import java.util.List;

/**
 * Puerto de salida (hexagonal): define lo que la capa de aplicacion
 * necesita del repositorio de empresas, sin saber como esta implementado.
 */
public interface EmpresaRepositoryPort {

    Empresa guardar(Empresa empresa);

    void actualizar(Empresa empresa);

    void eliminar(Long id);

    boolean existePorId(Long id);

    boolean existePorIdentificador(String identificador);

    default List<Empresa> listarTodos() {
        return List.of();
    }
}
