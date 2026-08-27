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

import java.util.Optional;

/**
 * Servicio de aplicacion para E2-HU01 (Registrar persona), E2-HU02 (asociar
 * empresa) y E2-HU03 (Consultar persona por documento).
 */
public class PersonaService {

    private static final String PERMISO_REGISTRAR_PERSONA = "registrar_persona";
    private static final String PERMISO_GESTIONAR_EMPRESAS = "gestionar_empresas";
    private static final String PERMISO_CONSULTAR_PERSONA = "consultar_persona";

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

    /**
     * Busca una persona por su documento (E2-HU03).
     * NOTA: la informacion de visita activa (Epica E3) y restricciones (Epica E7)
     * todavia no existe en el sistema. Cuando se implementen esas epicas, este
     * metodo (o quien lo llame) debera complementarse con esos datos.
     *
     * @param documento           documento de la persona a buscar
     * @param usuarioResponsable  username de quien realiza la busqueda (se valida su permiso)
     * @return la persona encontrada
     */
    public Persona consultarPersonaPorDocumento(String documento, String usuarioResponsable) {
        autorizacionService.verificarPermiso(usuarioResponsable, PERMISO_CONSULTAR_PERSONA);

        Optional<Persona> personaEncontrada = personaRepository.buscarPorDocumento(documento);

        return personaEncontrada.orElseThrow(() ->
                new PersonaNoEncontradaException("No existe ninguna persona registrada con el documento: " + documento)
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