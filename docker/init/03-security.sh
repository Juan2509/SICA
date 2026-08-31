#!/bin/bash
set -e

psql --set ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
     --set app_password="$SICA_APP_PASSWORD" <<-'EOSQL'
SELECT 'CREATE ROLE sica_aplicacion NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sica_aplicacion')
\gexec

SELECT 'CREATE ROLE sica_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION INHERIT'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sica_app')
\gexec

ALTER ROLE sica_app WITH PASSWORD :'app_password';
GRANT sica_aplicacion TO sica_app;

REVOKE ALL ON DATABASE sica_db FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE sica_db TO sica_aplicacion;
GRANT USAGE ON SCHEMA public TO sica_aplicacion;

GRANT SELECT ON
    roles, permisos, rol_permiso, usuarios, empresas, personas,
    visitas, incidentes, bitacora_auditoria
TO sica_aplicacion;

GRANT INSERT, UPDATE, DELETE ON
    rol_permiso, usuarios, empresas, personas, visitas, incidentes
TO sica_aplicacion;

GRANT INSERT ON bitacora_auditoria TO sica_aplicacion;
REVOKE UPDATE, DELETE, TRUNCATE ON bitacora_auditoria FROM sica_aplicacion;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sica_aplicacion;
REVOKE CREATE ON SCHEMA public FROM sica_aplicacion;
EOSQL
