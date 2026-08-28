package com.sica.visita.domain;

/**
 * Estados posibles de una visita, segun el documento del proyecto.
 * FINALIZADA se agrego en E4-HU02 para representar una salida normal
 * (distinta de CERRADA_POR_SISTEMA, que es para salidas olvidadas, Epica E6).
 */
public enum EstadoVisita {
    APROBADO,
    PENDIENTE_APROBACION,
    PENDIENTE_APROBACION_POR_OLVIDO,
    DENTRO,
    FINALIZADA,
    CERRADA_POR_SISTEMA
}