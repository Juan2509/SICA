USE sica_db;

-- Informacion adicional para identificar la entidad y el resultado auditado
ALTER TABLE bitacora_auditoria
    ADD COLUMN entidad VARCHAR(30) NOT NULL DEFAULT 'SISTEMA' AFTER accion,
    ADD COLUMN resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO' AFTER fecha;
