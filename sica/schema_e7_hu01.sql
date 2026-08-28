USE sica_db;

-- Tabla para registrar situaciones de seguridad
CREATE TABLE IF NOT EXISTS incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(500) NOT NULL,
    fecha_hora DATETIME NOT NULL,
    persona_id INT NULL,
    usuario_responsable VARCHAR(50) NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id)
);
