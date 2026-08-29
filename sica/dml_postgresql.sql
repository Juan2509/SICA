-- Ejemplos de manipulacion DML para PostgreSQL - E13-HU04
-- Los datos iniciales oficiales se encuentran en data.sql.

\set ON_ERROR_STOP on
BEGIN;

INSERT INTO empresas (nombre, identificador)
VALUES ('Empresa Temporal DML', 'TEMP-DML');

UPDATE empresas
SET nombre = 'Empresa Temporal Actualizada'
WHERE identificador = 'TEMP-DML';

DELETE FROM empresas
WHERE identificador = 'TEMP-DML';

COMMIT;

SELECT id, nombre FROM roles ORDER BY id;
SELECT username, rol_id, activo FROM usuarios ORDER BY id;
SELECT documento, nombre, estado_acceso FROM personas ORDER BY id;
SELECT id, invitado_id, estado FROM visitas ORDER BY id;
