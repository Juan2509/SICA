USE sica_db;

-- Permiso para consultar la trazabilidad del sistema
INSERT INTO permisos (nombre) VALUES ('consultar_bitacora');

-- El Administrador puede consultar la bitacora
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'consultar_bitacora'));
