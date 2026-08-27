package com.sica.persona.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.empresa.application.exception.EmpresaNoEncontradaException;
import com.sica.empresa.application.port.EmpresaRepositoryPort;
import com.sica.persona.application.exception.PersonaDuplicadaException;
import com.sica.persona.application.exception.PersonaInvalidaException;
import com.sica.persona.application.exception.PersonaNoEncontradaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.TipoPersona;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;

/**
 * Servicio de aplicacion para la Historia de Usuario E2-HU01 (Registrar persona)
 * y para la asociacion de una persona con una empresa (E2-HU02).
 */
public class PersonaService {

    private static final String PERMISO_REGISTRAR_PERSONA = "registrar_persona";
    private static final String PERMISO_GESTIONAR_EMPRESAS = "gestionar_empresas";

    private final PersonaRepositoryPort personaRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public PersonaService(PersonaRepositoryPort personaRepository, EmpresaRepositoryPort empresaRepository,
                           BitacoraAuditoriaPort bitacoraAuditoria, AutorizacionService autorizacionService) {
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Registra una nueva persona (trabajador o invitado) en el sistema.
     */
    public Persona registrarPersona(String nombre, String documento, TipoPersona tipo, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_REGISTRAR_PERSONA);

        validarDatosObligatorios(nombre, documento, tipo);

        if (personaRepository.existePorDocumento(documento)) {
            throw new PersonaDuplicadaException("Ya existe una persona registrada con el documento: " + documento);
        }

        Persona nuevaPersona = new Persona(nombre, documento, tipo);
        Persona personaGuardada = personaRepository.guardar(nuevaPersona);

        bitacoraAuditoria.registrar(
                "REGISTRAR_PERSONA",
                "Se registro la persona con documento: " + documento + " (tipo: " + tipo + ")",
                usuarioResponsable
        );

        return personaGuardada;
    }

    /**
     * Asocia una persona existente a una empresa existente.
     *
     * @param personaId           id de la persona
     * @param empresaId           id de la empresa
     * @param usuarioResponsable  username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     */
    public void asociarEmpresaAPersona(Long personaId, Long empresaId, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_GESTIONAR_EMPRESAS);

        if (!personaRepository.existePorId(personaId)) {
            throw new PersonaNoEncontradaException("No existe una persona con id: " + personaId);
        }
        if (!empresaRepository.existePorId(empresaId)) {
            throw new EmpresaNoEncontradaException("No existe una empresa con id: " + empresaId);
        }

        personaRepository.asociarEmpresa(personaId, empresaId);

        bitacoraAuditoria.registrar(
                "ASOCIAR_PERSONA_EMPRESA",
                "Se asocio la persona " + personaId + " a la empresa " + empresaId,
                usuarioResponsable
        );
    }

    private void validarDatosObligatorios(String nombre, String documento, TipoPersona tipo) {
        if (esVacio(nombre)) {
            throw new PersonaInvalidaException("El nombre es obligatorio.");
        }
        if (esVacio(documento)) {
            throw new PersonaInvalidaException("El documento es obligatorio.");
        }
        if (tipo == null) {
            throw new PersonaInvalidaException("El tipo de persona es obligatorio (TRABAJADOR o INVITADO).");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}