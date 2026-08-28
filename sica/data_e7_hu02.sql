USE sica_db;

-- El permiso bloquear_persona ya fue creado en data_hu02.sql.
-- El Administrador puede restringir o habilitar el acceso de una persona.
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'bloquear_persona'));
