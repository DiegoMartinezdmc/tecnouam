# API de inventario y préstamos

La [guía principal](../README.md) contiene la puesta en marcha completa de API y React, las variables de entorno, las cuentas de prueba y todos los endpoints.

## Ejecución y base de datos

Ejecutar con Java 25: `./mvnw spring-boot:run`. La API usa PostgreSQL. Por defecto se conecta a `jdbc:postgresql://localhost:5432/tecnouam-db` con el usuario `postgres`; configure `DB_URL`, `DB_USER` y `DB_PASSWORD` según su contenedor. `DB_PASSWORD` no tiene valor predeterminado. Puede copiar `.env.example` a `.env` y completar los secretos: Spring carga este archivo automáticamente al iniciar desde `api` o desde la raíz del proyecto. El archivo `.env` está excluido del repositorio.

Al arrancar, la API crea la base indicada en `DB_URL` si falta y el usuario tiene permiso `CREATEDB`. Spring ejecuta `src/main/resources/db/schema.sql` para crear las tablas `equipos` y `prestamos` con sus restricciones e índices si aún no existen; Hibernate valida que las columnas coincidan con las entidades. También puede preparar la base manualmente con `PGPASSWORD="$DB_PASSWORD" psql -v ON_ERROR_STOP=1 -h localhost -p 5432 -U postgres -d postgres -f db/create_database.sql` desde un equipo con `psql` disponible. El script es idempotente.

Si `psql` solo existe dentro del contenedor, ejecute `POSTGRES_CONTAINER=nombre_del_contenedor DB_PASSWORD="$DB_PASSWORD" bash db/init-container.sh` desde `api` en una terminal que tenga acceso a Docker. Puede definir `DB_USER` si el usuario del contenedor no es `postgres`.

Antes de iniciar, defina `ADMIN_PASSWORD`, `USER_PASSWORD` y `JWT_SECRET` en `api/.env` o en el entorno. La última debe contener al menos 32 bytes UTF-8; puede generar un valor con `openssl rand -hex 32`. Ninguna de estas claves tiene valor predeterminado en el repositorio.

## Cuentas de prueba y autenticación

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | `ADMIN` | `ADMIN_PASSWORD` de `api/.env` o del entorno |
| `user` | `USER` | `USER_PASSWORD` de `api/.env` o del entorno |

Para obtener un token, envíe `POST /api/auth/login` con `{"username":"user","password":"<contraseña>"}` o con el usuario `admin`. La respuesta incluye `token`, `tokenType` (`Bearer`) y `expiresIn` (3600 segundos). Envíe el token en `Authorization: Bearer <token>`. La API no crea sesiones. `USER` puede consultar; `ADMIN` también puede crear, actualizar, eliminar y devolver.

## Endpoints

| Método | Ruta | Permiso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| GET | `/api/equipos`, `/api/equipos/{id}` | USER, ADMIN |
| POST | `/api/equipos` | ADMIN |
| PUT | `/api/equipos/{id}` | ADMIN |
| DELETE | `/api/equipos/{id}` | ADMIN |
| GET | `/api/prestamos` | USER, ADMIN |
| POST | `/api/prestamos` | ADMIN |
| PUT | `/api/prestamos/{id}/devolver` | ADMIN |

Ejemplo de equipo: `{"codigoPatrimonial":"PAT-001","nombre":"Portátil Dell","tipo":"Computadora"}`. El estado se asigna automáticamente.

Ejemplo de préstamo: `{"equipoId":1,"nombreSolicitante":"Ana López","fechaPrestamo":"2026-10-28","fechaPrevistaDevolucion":"2026-11-04"}`. Las fechas usan formato ISO `AAAA-MM-DD`.

Al eliminar un equipo sin préstamos activos también se eliminan sus préstamos ya devueltos, pues la relación exige que cada préstamo tenga un equipo.

Sin token o con credenciales incorrectas se responde `401`; un token USER en una ruta de escritura recibe `403`.

## Demostración del reto 4

Abra los archivos `.http` con la extensión REST Client de VS Code. Cada uno es independiente y sus solicitudes deben enviarse en orden:

- [01-equipos-crud.http](01-equipos-crud.http): crear, listar, consultar, actualizar y eliminar equipos.
- [02-prestamos.http](02-prestamos.http): registrar préstamo, comprobar el estado `PRESTADO` y los conflictos de préstamo y eliminación.
- [03-devoluciones.http](03-devoluciones.http): devolver y comprobar que el equipo vuelve a `DISPONIBLE`.
- [04-seguridad.http](04-seguridad.http): login, JWT inválido, `401`, consultas USER y escrituras `403`.

Los archivos leen las contraseñas de `api/.env`, que está excluido del repositorio. Los JWT y los identificadores se toman de las respuestas anteriores dentro de cada archivo. Para comprobar la integración React, recargue la lista tras crear o prestar un equipo.
