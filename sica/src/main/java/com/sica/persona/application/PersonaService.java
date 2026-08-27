package com.sica.persona.application;

import com.sica.autorizacion.application.AutorizacionService;
import com.sica.persona.application.exception.PersonaDuplicadaException;
import com.sica.persona.application.exception.PersonaInvalidaException;
import com.sica.persona.application.port.PersonaRepositoryPort;
import com.sica.persona.domain.Persona;
import com.sica.persona.domain.TipoPersona;
import com.sica.usuario.application.port.BitacoraAuditoriaPort;

/**
 * Servicio de aplicacion para la Historia de Usuario E2-HU01 (Registrar persona).
 */
public class PersonaService {

    private static final String PERMISO_REGISTRAR_PERSONA = "registrar_persona";

    private final PersonaRepositoryPort personaRepository;
    private final BitacoraAuditoriaPort bitacoraAuditoria;
    private final AutorizacionService autorizacionService;

    public PersonaService(PersonaRepositoryPort personaRepository, BitacoraAuditoriaPort bitacoraAuditoria,
                           AutorizacionService autorizacionService) {
        this.personaRepository = personaRepository;
        this.bitacoraAuditoria = bitacoraAuditoria;
        this.autorizacionService = autorizacionService;
    }

    /**
     * Registra una nueva persona (trabajador o invitado) en el sistema.
     *
     * @param nombre              nombre completo de la persona (obligatorio)
     * @param documento           documento de identidad, unico (obligatorio)
     * @param tipo                TRABAJADOR o INVITADO (obligatorio)
     * @param usuarioResponsable  username de quien realiza la accion (se valida su permiso y queda en la bitacora)
     * @return la persona registrada, con su id ya asignado
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