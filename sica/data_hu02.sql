USE sica_db;

-- Permisos minimos mencionados en el documento del proyecto
INSERT INTO permisos (nombre) VALUES ('crear_usuario');
INSERT INTO permisos (nombre) VALUES ('registrar_visita');
INSERT INTO permisos (nombre) VALUES ('generar_reporte');
INSERT INTO permisos (nombre) VALUES ('bloquear_persona');

-- Ejemplo: el rol ADMINISTRADOR (id 1) puede crear usuarios
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (1, 1);