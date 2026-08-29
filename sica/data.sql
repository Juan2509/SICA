-- Datos iniciales oficiales de SICA para PostgreSQL
-- Ejecutar despues de schema.sql sobre la base sica_db.

\set ON_ERROR_STOP on
BEGIN;

INSERT INTO roles (nombre) VALUES
    ('ADMINISTRADOR'), ('GUARDA_SEGURIDAD'), ('FUNCIONARIO');

INSERT INTO permisos (nombre) VALUES
    ('crear_usuario'), ('actualizar_usuario'), ('eliminar_usuario'),
    ('administrar_roles'), ('registrar_persona'), ('actualizar_persona'),
    ('eliminar_persona'), ('consultar_persona'), ('bloquear_persona'),
    ('gestionar_empresas'), ('registrar_visita'), ('consultar_visita'),
    ('registrar_checkin'), ('registrar_checkout'),
    ('registrar_visitante_no_anunciado'), ('responder_solicitud_visita'),
    ('solicitar_ingreso_por_olvido'), ('registrar_incidente'),
    ('generar_reporte'), ('consultar_bitacora');

-- RBAC del administrador: todos los permisos.
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR';

-- RBAC del guarda: operaciones de acceso y seguridad.
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'GUARDA_SEGURIDAD'
AND p.nombre IN (
    'consultar_persona', 'consultar_visita', 'registrar_checkin',
    'registrar_checkout', 'registrar_visitante_no_anunciado',
    'solicitar_ingreso_por_olvido', 'registrar_incidente'
);

-- RBAC del funcionario: pre-registro y aprobaciones.
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'FUNCIONARIO'
AND p.nombre IN (
    'registrar_persona', 'gestionar_empresas', 'registrar_visita',
    'responder_solicitud_visita'
);

INSERT INTO empresas (nombre, identificador) VALUES
    ('Acme Tecnologia', 'NIT-900001'),
    ('Innovacion Central', 'NIT-900002');

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
    ('Persona Restringida', '20000003', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900002'),
        'https://example.com/fotos/persona-restringida.jpg', 'RESTRINGIDO'),
    ('Invitado Aprobado', '30000001', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-aprobado.jpg', 'HABILITADO'),
    ('Invitado Pendiente', '30000002', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-pendiente.jpg', 'HABILITADO');

-- Credenciales de ejemplo. LoginService aun compara texto directamente.
INSERT INTO usuarios (persona_id, username, password, rol_id, activo) VALUES
    ((SELECT id FROM personas WHERE documento = '10000001'), 'admin',
        'admin123', (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR'), TRUE),
    ((SELECT id FROM personas WHERE documento = '10000002'), 'guarda',
        'guarda123', (SELECT id FROM roles WHERE nombre = 'GUARDA_SEGURIDAD'), TRUE),
    ((SELECT id FROM personas WHERE documento = '10000003'), 'funcionario',
        'funcionario123', (SELECT id FROM roles WHERE nombre = 'FUNCIONARIO'), TRUE);

-- Invitado pre-registrado aprobado.
INSERT INTO visitas (invitado_id, persona_visitada_id, fecha_hora_visita, estado)
VALUES (
    (SELECT id FROM personas WHERE documento = '30000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP, 'APROBADO'
);

-- Invitado no anunciado pendiente.
INSERT INTO visitas (invitado_id, persona_visitada_id, fecha_hora_visita, estado)
VALUES (
    (SELECT id FROM personas WHERE documento = '30000002'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP, 'PENDIENTE_APROBACION'
);

-- Trabajador sin carnet pendiente por olvido.
INSERT INTO visitas (invitado_id, persona_visitada_id, fecha_hora_visita, estado)
VALUES (
    (SELECT id FROM personas WHERE documento = '20000002'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP, 'PENDIENTE_APROBACION_POR_OLVIDO'
);

-- Visita abierta para probar una salida olvidada.
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado,
    fecha_hora_checkin, usuario_checkin
) VALUES (
    (SELECT id FROM personas WHERE documento = '20000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    CURRENT_TIMESTAMP - INTERVAL '1 day', 'DENTRO',
    CURRENT_TIMESTAMP - INTERVAL '1 day', 'guarda'
);

INSERT INTO incidentes (descripcion, fecha_hora, persona_id, usuario_responsable)
VALUES (
    'Incidente de prueba para validar consultas y reportes', CURRENT_TIMESTAMP,
    (SELECT id FROM personas WHERE documento = '20000001'), 'guarda'
);

-- Permite probar la consulta de auditoria desde el primer inicio.
INSERT INTO bitacora_auditoria (
    accion, entidad, descripcion, usuario_responsable, fecha, resultado
) VALUES (
    'CARGAR_DATOS_INICIALES', 'SISTEMA',
    'Carga inicial de datos de prueba PostgreSQL',
    'SISTEMA', CURRENT_TIMESTAMP, 'EXITOSO'
);

COMMIT;
