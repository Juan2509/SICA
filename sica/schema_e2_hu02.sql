USE sica_db;

-- Tabla de empresas
CREATE TABLE IF NOT EXISTS empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    identificador VARCHAR(30) NOT NULL UNIQUE
);

-- Una persona puede asociarse con una empresa (puede quedar sin asociar por ahora)
ALTER TABLE personas
    ADD COLUMN empresa_id INT NULL,
    ADD FOREIGN KEY (empresa_id) REFERENCES empresas(id);