-- Base de datos del proyecto SICA
CREATE DATABASE IF NOT EXISTS sica_db;
USE sica_db;

-- Tabla de roles (se ira completando en las siguientes historias de E1)
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla de usuarios
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

-- Tabla de bitacora de auditoria
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    accion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    usuario_responsable VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL
);