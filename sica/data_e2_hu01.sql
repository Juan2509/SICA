USE sica_db;

-- Nuevo permiso para registrar personas
INSERT INTO permisos (nombre) VALUES ('registrar_persona');

-- El Funcionario (id 3) y el Administrador (id 1) pueden registrar personas
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (3, (SELECT id FROM permisos WHERE nombre = 'registrar_persona'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'registrar_persona'));