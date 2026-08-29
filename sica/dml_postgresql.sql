-- Datos iniciales y operaciones DML de SICA para PostgreSQL - E13-HU04
-- Ejecutar despues de ddl_postgresql.sql:
-- psql -U postgres -d sica_db -f dml_postgresql.sql

\set ON_ERROR_STOP on

BEGIN;

-- Roles iniciales
INSERT INTO roles (nombre) VALUES
    ('ADMINISTRADOR'),
    ('GUARDA_SEGURIDAD'),
    ('FUNCIONARIO');

-- Permisos utilizados por la aplicacion
INSERT INTO permisos (nombre) VALUES
    ('crear_usuario'),
    ('actualizar_usuario'),
    ('eliminar_usuario'),
    ('administrar_roles'),
    ('registrar_persona'),
    ('actualizar_persona'),
    ('eliminar_persona'),
    ('consultar_persona'),
    ('bloquear_persona'),
    ('gestionar_empresas'),
    ('registrar_visita'),
    ('consultar_visita'),
    ('registrar_checkin'),
    ('registrar_checkout'),
    ('registrar_visitante_no_anunciado'),
    ('responder_solicitud_visita'),
    ('solicitar_ingreso_por_olvido'),
    ('registrar_incidente'),
    ('generar_reporte'),
    ('consultar_bitacora');

-- El administrador recibe todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR';

-- Permisos del guarda
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'GUARDA_SEGURIDAD'
  AND p.nombre IN (
      'consultar_persona',
      'consultar_visita',
      'registrar_checkin',
      'registrar_checkout',
      'registrar_visitante_no_anunciado',
      'solicitar_ingreso_por_olvido',
      'registrar_incidente'
  );

-- Permisos del funcionario
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'FUNCIONARIO'
  AND p.nombre IN (
      'registrar_persona',
      'gestionar_empresas',
      'registrar_visita',
      'responder_solicitud_visita'
  );

-- Empresas del complejo
INSERT INTO empresas (nombre, identificador) VALUES
    ('Acme Tecnologia', 'NIT-900001'),
    ('Innovacion Central', 'NIT-900002');

-- Personas necesarias para cuentas y flujos de acceso
INSERT INTO personas (
    nombre, documento, tipo, empresa_id, foto_url, estado_acceso
) VALUES
    ('Administrador SICA', '10000001', 'TRABAJADOR', NULL,
        'https://example.com/fotos/admin.jpg', 'HABILITADO'),
    ('Guarda Principal', '10000002', 'TRABAJADOR', NULL,
        'https://example.com/fotos/guarda.jpg', 'HABILITADO'),
    ('Funcionario Acme', '10000003', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900001'),
        'https://example.com/fotos/funcionario.jpg', 'HABILITADO'),
    ('Trabajador de Prueba', '20000001', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900001'),
        'https://example.com/fotos/trabajador.jpg', 'HABILITADO'),
    ('Trabajador Sin Carnet', '20000002', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900002'),
        'https://example.com/fotos/trabajador-sin-carnet.jpg', 'HABILITADO'),
    ('Invitado Aprobado', '30000001', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-aprobado.jpg', 'HABILITADO'),
    ('Invitado Pendiente', '30000002', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-pendiente.jpg', 'HABILITADO');

-- Usuarios de prueba relacionados con personas, sin duplicar sus datos.
-- Las contrasenas son texto de prueba porque LoginService aun compara texto.
INSERT INTO usuarios (persona_id, username, password, rol_id, activo) VALUES
    ((SELECT id FROM personas WHERE documento = '10000001'),
        'admin', 'admin123',
        (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR'), TRUE),
    ((SELECT id FROM personas WHERE documento = '10000002'),
        'guarda', 'guarda123',
        (SELECT id FROM roles WHERE nombre = 'GUARDA_SEGURIDAD'), TRUE),
    ((SELECT id FROM personas WHERE documento = '10000003'),
        'funcionario', 'funcionario123',
        (SELECT id FROM roles WHERE nombre = 'FUNCIONARIO'), TRUE);

-- Invitado pre-registrado: listo para check-in
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado
) VALUES (
    (SELECT id FROM personas WHERE documento = '30000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP,
    'APROBADO'
);

-- Invitado no anunciado: pendiente de respuesta del funcionario
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado
) VALUES (
    (SELECT id FROM personas WHERE documento = '30000002'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP,
    'PENDIENTE_APROBACION'
);

-- Trabajador sin carnet: pendiente de aprobacion por olvido
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado
) VALUES (
    (SELECT id FROM personas WHERE documento = '20000002'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP,
    'PENDIENTE_APROBACION_POR_OLVIDO'
);

-- Visita abierta para probar la regularizacion de una salida olvidada
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado,
    fecha_hora_checkin, usuario_checkin
) VALUES (
    (SELECT id FROM personas WHERE documento = '20000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    'DENTRO',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    'guarda'
);

INSERT INTO incidentes (
    descripcion, fecha_hora, persona_id, usuario_responsable
) VALUES (
    'Incidente de prueba para validar consultas y reportes',
    CURRENT_TIMESTAMP,
    (SELECT id FROM personas WHERE documento = '20000001'),
    'guarda'
);

-- Ejemplos controlados de UPDATE y DELETE. El registro temporal se elimina
-- antes de confirmar, por lo que no altera los datos iniciales definitivos.
INSERT INTO empresas (nombre, identificador)
VALUES ('Empresa Temporal DML', 'TEMP-DML');

UPDATE empresas
SET nombre = 'Empresa Temporal Actualizada'
WHERE identificador = 'TEMP-DML';

DELETE FROM empresas
WHERE identificador = 'TEMP-DML';

COMMIT;

-- Consultas SELECT de verificacion
SELECT id, nombre FROM roles ORDER BY id;
SELECT id, nombre FROM permisos ORDER BY id;
SELECT username, rol_id, activo FROM usuarios ORDER BY id;
SELECT documento, nombre, tipo, estado_acceso FROM personas ORDER BY id;
SELECT id, invitado_id, persona_visitada_id, estado FROM visitas ORDER BY id;
