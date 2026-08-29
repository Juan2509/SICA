-- Transacciones PostgreSQL para SICA - E13-HU06
-- Requiere haber ejecutado ddl_postgresql.sql y data.sql.
-- Ejecutar con: psql -U postgres -d sica_db -f tcl_postgresql.sql

\set ON_ERROR_STOP on

-- ================================================================
-- Transaccion 1: regularizar una salida olvidada y confirmar cambios
-- ================================================================
BEGIN;

-- FOR UPDATE evita que otra transaccion cambie la misma visita mientras
-- se realiza la regularizacion. \gset guarda los valores como variables psql.
SELECT
    v.id AS visita_anterior_id,
    v.invitado_id AS persona_ingreso_id,
    v.persona_visitada_id AS funcionario_id
FROM visitas v
INNER JOIN personas p ON p.id = v.invitado_id
WHERE p.documento = '20000001'
  AND v.estado = 'DENTRO'
ORDER BY v.fecha_hora_visita DESC
LIMIT 1
FOR UPDATE OF v
\gset

-- Si una operacion posterior falla durante una ejecucion interactiva, se puede
-- volver a este punto con: ROLLBACK TO SAVEPOINT antes_regularizacion;
SAVEPOINT antes_regularizacion;

UPDATE visitas
SET estado = 'CERRADA_POR_SISTEMA',
    fecha_hora_checkout = CURRENT_TIMESTAMP,
    usuario_checkout = 'SISTEMA'
WHERE id = :visita_anterior_id;

INSERT INTO visitas (
    invitado_id,
    persona_visitada_id,
    fecha_hora_visita,
    estado,
    fecha_hora_checkin,
    usuario_checkin
) VALUES (
    :persona_ingreso_id,
    :funcionario_id,
    CURRENT_TIMESTAMP,
    'DENTRO',
    CURRENT_TIMESTAMP,
    'guarda'
)
RETURNING id AS nueva_visita_id
\gset

INSERT INTO bitacora_auditoria (
    accion,
    entidad,
    descripcion,
    usuario_responsable,
    fecha,
    resultado
) VALUES (
    'REGULARIZAR_SALIDA_OLVIDADA',
    'VISITA',
    'Se cerro la visita ' || :visita_anterior_id
        || ' y se creo la visita ' || :nueva_visita_id,
    'guarda',
    CURRENT_TIMESTAMP,
    'EXITOSO'
);

COMMIT;

-- ================================================================
-- Transaccion 2: ejemplo seguro de ROLLBACK
-- ================================================================
-- El cambio se ejecuta y luego se revierte. Sirve para comprobar que una
-- operacion fallida no deja datos parciales.
BEGIN;

SAVEPOINT antes_cambio_prueba;

UPDATE personas
SET estado_acceso = 'RESTRINGIDO'
WHERE documento = '30000001';

ROLLBACK TO SAVEPOINT antes_cambio_prueba;
ROLLBACK;

-- La persona conserva HABILITADO porque la segunda transaccion se revirtio.
SELECT documento, estado_acceso
FROM personas
WHERE documento = '30000001';
