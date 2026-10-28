-- Ejecutar con psql conectado a la base de mantenimiento postgres.
-- \gexec ejecuta CREATE DATABASE solo cuando la base tecnouam-db no existe.
SELECT 'CREATE DATABASE "tecnouam-db"'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'tecnouam-db')
\gexec

\connect tecnouam-db
\ir ../src/main/resources/db/schema.sql
