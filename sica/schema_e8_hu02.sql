USE sica_db;

-- Impide modificar registros historicos de la bitacora
DELIMITER //

CREATE TRIGGER bloquear_actualizacion_bitacora
BEFORE UPDATE ON bitacora_auditoria
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los registros de auditoria no se pueden modificar';
END //

-- Impide eliminar registros historicos de la bitacora
CREATE TRIGGER bloquear_eliminacion_bitacora
BEFORE DELETE ON bitacora_auditoria
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los registros de auditoria no se pueden eliminar';
END //

DELIMITER ;
