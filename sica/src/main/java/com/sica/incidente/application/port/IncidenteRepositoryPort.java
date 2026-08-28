package com.sica.incidente.application.port;

import com.sica.incidente.domain.Incidente;

/**
 * Puerto de salida para guardar incidentes sin depender de JDBC.
 */
public interface IncidenteRepositoryPort {

    Incidente guardar(Incidente incidente);
}
