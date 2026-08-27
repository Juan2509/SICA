package com.sica.visita.domain;

/**
 * Estados posibles de una visita, segun el documento del proyecto.
 * En esta historia (E3-HU01) solo se usa APROBADO; los demas se
 * usaran en las siguientes historias de las epicas E3, E5 y E6.
 */
public enum EstadoVisita {
    APROBADO,
    PENDIENTE_APROBACION,
    PENDIENTE_APROBACION_POR_OLVIDO,
    DENTRO,
    CERRADA_POR_SISTEMA
}