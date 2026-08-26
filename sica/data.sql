USE sica_db;

-- Roles minimos para poder crear usuarios de prueba
INSERT INTO roles (nombre) VALUES ('ADMINISTRADOR');
INSERT INTO roles (nombre) VALUES ('GUARDA_SEGURIDAD');
INSERT INTO roles (nombre) VALUES ('FUNCIONARIO');