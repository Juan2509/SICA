USE sica_db;

-- Permiso para registrar incidentes de seguridad
INSERT INTO permisos (nombre) VALUES ('registrar_incidente');

-- El Guarda de Seguridad puede registrar incidentes
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'registrar_incidente'));

-- El Administrador tambien puede registrar incidentes
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'registrar_incidente'));
