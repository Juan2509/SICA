-- Esquema completo del proyecto SICA
CREATE DATABASE IF NOT EXISTS sica_db;
USE sica_db;

-- Roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Permisos individuales utilizados por RBAC
CREATE TABLE IF NOT EXISTS permisos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Relacion muchos a muchos entre roles y permisos
CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id INT NOT NULL,
    permiso_id INT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id),
    FOREIGN KEY (permiso_id) REFERENCES permisos(id)
);

-- Usuarios que pueden iniciar sesion en SICA
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(30) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- Empresas ubicadas dentro del complejo
CREATE TABLE IF NOT EXISTS empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    identificador VARCHAR(30) NOT NULL UNIQUE
);

-- Trabajadores e invitados identificados por documento
CREATE TABLE IF NOT EXISTS personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL,
    empresa_id INT NULL,
    foto_url VARCHAR(255) NULL,
    estado_acceso VARCHAR(20) NOT NULL DEFAULT 'HABILITADO',
    FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

-- Visitas y registros de entrada y salida
CREATE TABLE IF NOT EXISTS visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invitado_id INT NOT NULL,
    persona_visitada_id INT NOT NULL,
    fecha_hora_visita DATETIME NOT NULL,
    estado VARCHAR(40) NOT NULL,
    fecha_hora_checkin DATETIME NULL,
    usuario_checkin VARCHAR(50) NULL,
    fecha_hora_checkout DATETIME NULL,
    usuario_checkout VARCHAR(50) NULL,
    FOREIGN KEY (invitado_id) REFERENCES personas(id),
    FOREIGN KEY (persona_visitada_id) REFERENCES personas(id)
);

-- Incidentes de seguridad, con una persona asociada cuando corresponda
CREATE TABLE IF NOT EXISTS incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(500) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    persona_id INT NULL,
    usuario_responsable VARCHAR(50) NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id)
);

-- Registro historico de acciones relevantes
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(30) NOT NULL DEFAULT 'SISTEMA',
    descripcion VARCHAR(255) NOT NULL,
    usuario_responsable VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL,
    resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO'
);

-- La bitacora es inmutable: solamente permite insertar y consultar
DROP TRIGGER IF EXISTS bloquear_actualizacion_bitacora;
DROP TRIGGER IF EXISTS bloquear_eliminacion_bitacora;

DELIMITER //

CREATE TRIGGER bloquear_actualizacion_bitacora
BEFORE UPDATE ON bitacora_auditoria
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los registros de auditoria no se pueden modificar';
END //

CREATE TRIGGER bloquear_eliminacion_bitacora
BEFORE DELETE ON bitacora_auditoria
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los registros de auditoria no se pueden eliminar';
END //

DELIMITER ;
