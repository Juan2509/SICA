-- DDL principal de SICA para PostgreSQL - E13-HU03
-- Ejecutar con psql desde esta carpeta:
-- psql -U postgres -f ddl_postgresql.sql

\set ON_ERROR_STOP on

-- CREATE DATABASE no admite IF NOT EXISTS en PostgreSQL. \gexec ejecuta
-- la instruccion solamente cuando la base aun no existe.
SELECT 'CREATE DATABASE sica_db'
WHERE NOT EXISTS (
    SELECT 1 FROM pg_database WHERE datname = 'sica_db'
) \gexec

\connect sica_db

-- Carga la estructura completa usando una ruta relativa a este archivo.
\ir schema.sql
