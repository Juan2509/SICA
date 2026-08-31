package com.sica.empresa.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.empresa.application.exception.EmpresaDuplicadaException;
import com.sica.empresa.application.exception.EmpresaInvalidaException;
import com.sica.empresa.application.exception.EmpresaNoEncontradaException;
import com.sica.empresa.application.port.EmpresaRepositoryPort;
import com.sica.empresa.domain.Empresa;
import com.sica.auditoria.application.port.BitacoraAuditoriaPort;
import java.util.List;

/**
 * Servicio de aplicacion para la Historia de Usuario E2-HU02 (Registrar empresa).
 * Crear, actualizar y eliminar una empresa quedan registrados en la bitacora de auditoria.
 */
public class EmpresaService {

    private static final String PERMISO_GESTIONAR_EMPRESAS = "gestionar_empresas";

    private final EmpresaRepositoryPort empresaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public EmpresaService(EmpresaRepositoryPort empresaRepository, BitacoraAuditoriaPort bitacoraAuditoria,
                           AutorizacionService autorizacionService) {
        this.empresaRepository = empresaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Registra una nueva empresa.
     *
     * @param nombre              nombre de la empresa (obligatorio)
     * @param identificador       identificador unico de la empresa, ej. NIT (obligatorio)
     * @param usuarioResponsable  username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     */
    public Empresa registrarEmpresa(String nombre, String identificador, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GESTIONAR_EMPRESAS);

        validarDatosObligatorios(nombre, identificador);

        if (empresaRepository.existePorIdentificador(identificador)) {
            throw new EmpresaDuplicadaException("Ya existe una empresa con el identificador: " + identificador);
        }

        Empresa nuevaEmpresa = new Empresa(nombre, identificador);
        Empresa empresaGuardada = empresaRepository.guardar(nuevaEmpresa);

        bitacoraAuditoria.registrar(
                "REGISTRAR_EMPRESA",
                "Se registro la empresa con identificador: " + identificador,
                usuarioResponsable
        );

        return empresaGuardada;
    }

    public List<Empresa> consultarEmpresas(String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GESTIONAR_EMPRESAS);
        return empresaRepository.listarTodos();
    }

    /**
     * Actualiza el nombre y/o identificador de una empresa existente.
     */
    public void actualizarEmpresa(Long id, String nombre, String identificador, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GESTIONAR_EMPRESAS);

        if (!empresaRepository.existePorId(id)) {
            throw new EmpresaNoEncontradaException("No existe una empresa con id: " + id);
        }

        validarDatosObligatorios(nombre, identificador);

        Empresa empresa = new Empresa(nombre, identificador);
        empresa.setId(id);
        empresaRepository.actualizar(empresa);

        bitacoraAuditoria.registrar(
                "ACTUALIZAR_EMPRESA",
                "Se actualizo la empresa con id: " + id,
                usuarioResponsable
        );
    }

    /**
     * Elimina una empresa existente.
     */
    public void eliminarEmpresa(Long id, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GESTIONAR_EMPRESAS);

        if (!empresaRepository.existePorId(id)) {
            throw new EmpresaNoEncontradaException("No existe una empresa con id: " + id);
        }

        empresaRepository.eliminar(id);

        bitacoraAuditoria.registrar(
                "ELIMINAR_EMPRESA",
                "Se elimino la empresa con id: " + id,
                usuarioResponsable
        );
    }

    private void validarDatosObligatorios(String nombre, String identificador) {
        if (esVacio(nombre)) {
            throw new EmpresaInvalidaException("El nombre es obligatorio.");
        }
        if (esVacio(identificador)) {
            throw new EmpresaInvalidaException("El identificador es obligatorio.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
