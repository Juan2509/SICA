USE sica_db;

-- Permiso para solicitar el ingreso de un trabajador que olvido su carnet
INSERT INTO permisos (nombre) VALUES ('solicitar_ingreso_por_olvido');

-- El Guarda de Seguridad crea la solicitud
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'solicitar_ingreso_por_olvido'));

-- El Administrador tambien puede crear la solicitud
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'solicitar_ingreso_por_olvido'));
