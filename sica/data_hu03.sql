USE sica_db;

-- Nuevo permiso requerido para asociar permisos a roles (E1-HU02, ahora validado en E1-HU03)
INSERT INTO permisos (nombre) VALUES ('administrar_roles');

-- El rol ADMINISTRADOR (id 1) puede administrar roles
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'administrar_roles'));