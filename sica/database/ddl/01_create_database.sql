-- Crea la base y carga la estructura oficial.
\set ON_ERROR_STOP on

SELECT 'CREATE DATABASE sica_db'
WHERE NOT EXISTS (
    SELECT 1 FROM pg_database WHERE datname = 'sica_db'
) \gexec

\connect sica_db
\ir ../../schema.sql
