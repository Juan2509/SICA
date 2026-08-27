USE sica_db;

-- Nuevo permiso para registrar, actualizar, eliminar empresas y asociarlas con personas
INSERT INTO permisos (nombre) VALUES ('gestionar_empresas');

-- El Administrador (id 1) y el Funcionario (id 3) pueden gestionar empresas
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'gestionar_empresas'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (3, (SELECT id FROM permisos WHERE nombre = 'gestionar_empresas'));