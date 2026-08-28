USE sica_db;

-- Permisos necesarios para el flujo de invitado no anunciado
INSERT INTO permisos (nombre) VALUES ('registrar_visitante_no_anunciado');
INSERT INTO permisos (nombre) VALUES ('responder_solicitud_visita');

-- El Guarda de Seguridad registra la solicitud
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'registrar_visitante_no_anunciado'));

-- El Funcionario responde la solicitud
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (3, (SELECT id FROM permisos WHERE nombre = 'responder_solicitud_visita'));

-- El Administrador puede realizar ambas acciones
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'registrar_visitante_no_anunciado'));

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'responder_solicitud_visita'));
