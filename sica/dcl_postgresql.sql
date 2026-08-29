-- Privilegios PostgreSQL para SICA - E13-HU05
-- Ejecutar una sola vez como administrador, despues del DDL:
-- psql -U postgres -d sica_db -f dcl_postgresql.sql

\set ON_ERROR_STOP on

-- Rol grupal: recibe privilegios, pero no puede iniciar sesion.
CREATE ROLE sica_aplicacion NOLOGIN;

-- Usuario tecnico usado solamente por la aplicacion Java.
-- No es superusuario y no puede crear bases, roles ni replicaciones.
CREATE ROLE sica_app
WITH LOGIN
NOSUPERUSER
NOCREATEDB
NOCREATEROLE
NOREPLICATION
INHERIT
IN ROLE sica_aplicacion;

-- Solicita la contrasena de forma interactiva para no guardarla en Git.
\password sica_app

-- Se retiran privilegios publicos innecesarios.
REVOKE ALL ON DATABASE sica_db FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;

-- Acceso minimo para conectarse y localizar las tablas.
GRANT CONNECT ON DATABASE sica_db TO sica_aplicacion;
GRANT USAGE ON SCHEMA public TO sica_aplicacion;

-- Lectura requerida para autenticacion, RBAC, consultas y reportes.
GRANT SELECT ON
    roles,
    permisos,
    rol_permiso,
    usuarios,
    empresas,
    personas,
    visitas,
    incidentes,
    bitacora_auditoria
TO sica_aplicacion;

-- Escritura sobre las entidades que administran los servicios actuales.
GRANT INSERT, UPDATE, DELETE ON
    rol_permiso,
    usuarios,
    empresas,
    personas,
    visitas,
    incidentes
TO sica_aplicacion;

-- La aplicacion puede agregar auditoria, pero no modificarla ni eliminarla.
GRANT INSERT ON bitacora_auditoria TO sica_aplicacion;
REVOKE UPDATE, DELETE, TRUNCATE ON bitacora_auditoria FROM sica_aplicacion;

-- Permite obtener valores de las columnas IDENTITY al insertar registros.
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sica_aplicacion;

-- Defensa adicional: la cuenta tecnica nunca puede cambiar la estructura.
REVOKE CREATE ON SCHEMA public FROM sica_aplicacion;
