USE sica_db;

-- Permisos para completar las operaciones auditables de usuarios y personas
INSERT INTO permisos (nombre) VALUES ('actualizar_usuario');
INSERT INTO permisos (nombre) VALUES ('eliminar_usuario');
INSERT INTO permisos (nombre) VALUES ('actualizar_persona');
INSERT INTO permisos (nombre) VALUES ('eliminar_persona');

-- El Administrador puede actualizar y eliminar usuarios y personas
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'actualizar_usuario'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'eliminar_usuario'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'actualizar_persona'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'eliminar_persona'));
