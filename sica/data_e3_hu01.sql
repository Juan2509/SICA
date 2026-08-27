USE sica_db;

-- El permiso 'registrar_visita' ya se creo en data_hu02.sql pero aun no estaba asociado a ningun rol
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (3, (SELECT id FROM permisos WHERE nombre = 'registrar_visita')); -- FUNCIONARIO

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'registrar_visita')); -- ADMINISTRADOR

-- Nuevo permiso para que el guarda pueda consultar visitas
INSERT INTO permisos (nombre) VALUES ('consultar_visita');

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (2, (SELECT id FROM permisos WHERE nombre = 'consultar_visita')); -- GUARDA_SEGURIDAD

INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'consultar_visita')); -- ADMINISTRADOR