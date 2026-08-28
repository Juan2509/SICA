USE sica_db;

-- Estado que permite restringir o habilitar el ingreso de una persona
ALTER TABLE personas
    ADD COLUMN estado_acceso VARCHAR(20) NOT NULL DEFAULT 'HABILITADO';
