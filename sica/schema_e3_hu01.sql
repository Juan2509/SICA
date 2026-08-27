USE sica_db;

-- Tabla de visitas
CREATE TABLE IF NOT EXISTS visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invitado_id INT NOT NULL,
    persona_visitada_id INT NOT NULL,
    fecha_hora_visita DATETIME NOT NULL,
    estado VARCHAR(40) NOT NULL,
    FOREIGN KEY (invitado_id) REFERENCES personas(id),
    FOREIGN KEY (persona_visitada_id) REFERENCES personas(id)
);