USE sica_db;

-- Tabla de personas (trabajadores e invitados)
CREATE TABLE IF NOT EXISTS personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    tipo VARCHAR(20) NOT NULL
);