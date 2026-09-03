-- Migra una base SICA existente para proteger la cuenta administrativa principal.
\set ON_ERROR_STOP on

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS es_administrador_principal BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE usuarios
SET es_administrador_principal = TRUE
WHERE username = 'admin';

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuarios_administrador_principal
    ON usuarios (es_administrador_principal)
    WHERE es_administrador_principal = TRUE;
