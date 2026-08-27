USE sica_db;

-- Nuevo permiso para consultar una persona por documento
INSERT INTO permisos (nombre) VALUES ('consultar_persona');

-- El Guarda de Seguridad (id 2) y el Administrador (id 1) pueden consultar personas
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'consultar_persona'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'consultar_persona'));