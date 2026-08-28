USE sica_db;

-- El permiso generar_reporte ya fue creado en data_hu02.sql.
-- El Administrador puede generar reportes del sistema.
INSERT INTO rol_permiso (rol_id, permiso_id)
VALUES (1, (SELECT id FROM permisos WHERE nombre = 'generar_reporte'));
