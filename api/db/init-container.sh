#!/usr/bin/env bash
set -euo pipefail

# Ejecutar desde un equipo con Docker CLI conectado al contenedor PostgreSQL.
: "${POSTGRES_CONTAINER:?Defina POSTGRES_CONTAINER con el nombre del contenedor}"
: "${DB_PASSWORD:?Defina DB_PASSWORD con la contraseña de PostgreSQL}"
DB_USER="${DB_USER:-postgres}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$POSTGRES_CONTAINER" \
  psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d postgres <<'SQL'
SELECT 'CREATE DATABASE "tecnouam-db"'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'tecnouam-db')
\gexec
SQL

docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$POSTGRES_CONTAINER" \
  psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d tecnouam-db \
  < "$SCRIPT_DIR/../src/main/resources/db/schema.sql"

echo 'Base tecnouam-db y tablas equipos/prestamos listas.'
