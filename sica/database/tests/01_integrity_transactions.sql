-- Pruebas de integridad y transacciones PostgreSQL - E13-HU12
-- Requiere schema.sql y data.sql ejecutados previamente.
-- Ejecutar: psql -U postgres -d sica_db -f database/tests/01_integrity_transactions.sql

\set ON_ERROR_STOP on

-- 1. PostgreSQL debe rechazar una clave foranea inexistente.
DO $$
BEGIN
    BEGIN
        INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (-1, -1);
        RAISE EXCEPTION 'FALLO: PostgreSQL acepto una FK inexistente';
    EXCEPTION
        WHEN foreign_key_violation THEN
            RAISE NOTICE 'OK: FK inexistente rechazada';
    END;
END;
$$;

-- 2. PostgreSQL debe rechazar una clave primaria duplicada.
DO $$
DECLARE
    id_existente BIGINT;
BEGIN
    SELECT id INTO id_existente FROM roles ORDER BY id LIMIT 1;
    BEGIN
        INSERT INTO roles (id, nombre) VALUES (id_existente, 'ROL_PK_DUPLICADA');
        RAISE EXCEPTION 'FALLO: PostgreSQL acepto una PK duplicada';
    EXCEPTION
        WHEN unique_violation THEN
            RAISE NOTICE 'OK: PK duplicada rechazada';
    END;
END;
$$;

-- 3. PostgreSQL debe rechazar un dato obligatorio NULL.
DO $$
BEGIN
    BEGIN
        INSERT INTO permisos (nombre) VALUES (NULL);
        RAISE EXCEPTION 'FALLO: PostgreSQL acepto un dato obligatorio NULL';
    EXCEPTION
        WHEN not_null_violation THEN
            RAISE NOTICE 'OK: dato obligatorio NULL rechazado';
    END;
END;
$$;

-- 4. PostgreSQL debe rechazar valores fuera de los estados permitidos.
DO $$
BEGIN
    BEGIN
        INSERT INTO personas (nombre, documento, tipo, estado_acceso)
        VALUES ('Persona Invalida', 'DOC-INVALIDO', 'TIPO_INVALIDO', 'HABILITADO');
        RAISE EXCEPTION 'FALLO: PostgreSQL acepto un tipo de persona invalido';
    EXCEPTION
        WHEN check_violation THEN
            RAISE NOTICE 'OK: valor invalido rechazado';
    END;
END;
$$;

-- 5. PostgreSQL debe rechazar documentos duplicados.
DO $$
DECLARE
    documento_existente VARCHAR(30);
BEGIN
    SELECT documento INTO documento_existente FROM personas ORDER BY id LIMIT 1;
    BEGIN
        INSERT INTO personas (nombre, documento, tipo, estado_acceso)
        VALUES ('Documento Duplicado', documento_existente, 'TRABAJADOR', 'HABILITADO');
        RAISE EXCEPTION 'FALLO: PostgreSQL acepto un documento duplicado';
    EXCEPTION
        WHEN unique_violation THEN
            RAISE NOTICE 'OK: documento duplicado rechazado';
    END;
END;
$$;

-- 6. ROLLBACK: la empresa temporal no debe conservarse.
BEGIN;
INSERT INTO empresas (nombre, identificador)
VALUES ('Empresa para Rollback', 'QA-ROLLBACK');
ROLLBACK;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM empresas WHERE identificador = 'QA-ROLLBACK') THEN
        RAISE EXCEPTION 'FALLO: ROLLBACK no revirtio la empresa temporal';
    END IF;
    RAISE NOTICE 'OK: ROLLBACK revirtio todos los cambios';
END;
$$;

-- 7. COMMIT: el dato debe existir despues de confirmar.
BEGIN;
INSERT INTO empresas (nombre, identificador)
VALUES ('Empresa para Commit', 'QA-COMMIT');
COMMIT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM empresas WHERE identificador = 'QA-COMMIT') THEN
        RAISE EXCEPTION 'FALLO: COMMIT no conservo la empresa temporal';
    END IF;
    RAISE NOTICE 'OK: COMMIT conservo los cambios';
END;
$$;

DELETE FROM empresas WHERE identificador = 'QA-COMMIT';

-- 8. SAVEPOINT: revierte solo el cambio posterior al punto de guardado.
BEGIN;
INSERT INTO empresas (nombre, identificador)
VALUES ('Nombre antes del Savepoint', 'QA-SAVEPOINT');

SAVEPOINT antes_de_actualizar;

UPDATE empresas
SET nombre = 'Nombre que debe revertirse'
WHERE identificador = 'QA-SAVEPOINT';

ROLLBACK TO SAVEPOINT antes_de_actualizar;
COMMIT;

DO $$
DECLARE
    nombre_guardado VARCHAR(100);
BEGIN
    SELECT nombre INTO nombre_guardado
    FROM empresas WHERE identificador = 'QA-SAVEPOINT';

    IF nombre_guardado <> 'Nombre antes del Savepoint' THEN
        RAISE EXCEPTION 'FALLO: SAVEPOINT no revirtio solamente la actualizacion';
    END IF;
    RAISE NOTICE 'OK: SAVEPOINT revirtio el cambio parcial';
END;
$$;

DELETE FROM empresas WHERE identificador = 'QA-SAVEPOINT';

SELECT 'TODAS LAS PRUEBAS DE INTEGRIDAD Y TRANSACCIONES PASARON' AS resultado;
