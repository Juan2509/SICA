USE sica_db;

-- Roles iniciales
INSERT INTO roles (nombre) VALUES
    ('ADMINISTRADOR'),
    ('GUARDA_SEGURIDAD'),
    ('FUNCIONARIO');

-- Permisos utilizados por las operaciones actuales de SICA
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

-- El Administrador tiene todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'ADMINISTRADOR';

-- Permisos del Guarda de Seguridad
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

-- Permisos del Funcionario
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

-- Usuarios de ejemplo. Las contrasenas se guardan como texto porque
-- el LoginService actual todavia realiza una comparacion directa.
INSERT INTO usuarios (nombre, documento, username, password, rol_id, activo) VALUES
    ('Administrador SICA', '10000001', 'admin', 'admin123',
        (SELECT id FROM roles WHERE nombre = 'ADMINISTRADOR'), TRUE),
    ('Guarda Principal', '10000002', 'guarda', 'guarda123',
        (SELECT id FROM roles WHERE nombre = 'GUARDA_SEGURIDAD'), TRUE),
    ('Funcionario Acme', '10000003', 'funcionario', 'funcionario123',
        (SELECT id FROM roles WHERE nombre = 'FUNCIONARIO'), TRUE);

-- Empresas de ejemplo
INSERT INTO empresas (nombre, identificador) VALUES
    ('Acme Tecnologia', 'NIT-900001'),
    ('Innovacion Central', 'NIT-900002');

-- Personas de ejemplo. El documento del funcionario coincide con su usuario,
-- lo cual permite recibir y responder solicitudes de aprobacion.
INSERT INTO personas (nombre, documento, tipo, empresa_id, foto_url, estado_acceso) VALUES
    ('Funcionario Acme', '10000003', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900001'),
        'https://example.com/fotos/funcionario.jpg', 'HABILITADO'),
    ('Trabajador de Prueba', '20000001', 'TRABAJADOR',
        (SELECT id FROM empresas WHERE identificador = 'NIT-900001'),
        'https://example.com/fotos/trabajador.jpg', 'HABILITADO'),
    ('Invitado Aprobado', '30000001', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-aprobado.jpg', 'HABILITADO'),
    ('Invitado Pendiente', '30000002', 'INVITADO', NULL,
        'https://example.com/fotos/invitado-pendiente.jpg', 'HABILITADO');

-- Visita aprobada para probar un check-in normal
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado
) VALUES (
    (SELECT id FROM personas WHERE documento = '30000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    NOW(),
    'APROBADO'
);

-- Solicitud pendiente para probar aprobacion o rechazo
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado
) VALUES (
    (SELECT id FROM personas WHERE documento = '30000002'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    NOW(),
    'PENDIENTE_APROBACION'
);

-- Visita abierta para probar deteccion de una salida olvidada
INSERT INTO visitas (
    invitado_id, persona_visitada_id, fecha_hora_visita, estado,
    fecha_hora_checkin, usuario_checkin
) VALUES (
    (SELECT id FROM personas WHERE documento = '20000001'),
    (SELECT id FROM personas WHERE documento = '10000003'),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'DENTRO',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    'guarda'
);

-- Incidente de ejemplo asociado al trabajador
INSERT INTO incidentes (descripcion, fecha_hora, persona_id, usuario_responsable) VALUES (
    'Incidente de prueba para validar consultas y reportes',
    NOW(),
    (SELECT id FROM personas WHERE documento = '20000001'),
    'guarda'
);
