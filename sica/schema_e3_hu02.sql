USE sica_db;

-- La foto del visitante se muestra mediante URL (E3-HU02)
ALTER TABLE personas
    ADD COLUMN foto_url VARCHAR(255) NULL;