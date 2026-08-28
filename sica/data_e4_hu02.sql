USE sica_db;

-- Nuevo permiso para registrar el check-out de una visita
INSERT INTO permisos (nombre) VALUES ('registrar_checkout');

-- El Guarda de Seguridad (id 2) y el Administrador (id 1) pueden registrar check-out
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'registrar_checkout'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'registrar_checkout'));