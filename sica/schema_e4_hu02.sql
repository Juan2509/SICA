USE sica_db;

-- Registro de fecha/hora y responsable del check-out (E4-HU02)
ALTER TABLE visitas
    ADD COLUMN fecha_hora_checkout DATETIME NULL,
    ADD COLUMN usuario_checkout VARCHAR(50) NULL;