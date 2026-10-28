# TecnoUAM

Aplicación de inventario y préstamos de equipos tecnológicos. Incluye una API Spring Boot con PostgreSQL y JWT, y un cliente React que consulta los equipos reales mediante Axios.

## Requisitos

- Java 25 y el wrapper Maven incluido en `api`.
- Node.js y npm para el cliente React.
- Un contenedor PostgreSQL accesible desde el equipo donde se ejecuta la API. La configuración predeterminada usa `localhost:5432`, usuario `postgres` y base `tecnouam-db`.

## Variables de entorno

Copie `api/.env.example` a `api/.env` y complete los valores. Spring Boot carga `api/.env` al iniciarse desde `api` o desde la raíz del proyecto. El archivo `.env` está excluido del repositorio.

| Variable | Requerida | Uso |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña del usuario PostgreSQL. |
| `ADMIN_PASSWORD` | Sí | Contraseña de la cuenta de prueba `admin`. |
| `USER_PASSWORD` | Sí | Contraseña de la cuenta de prueba `user`. |
| `JWT_SECRET` | Sí | Clave de firma JWT de al menos 32 bytes UTF-8. |
| `DB_URL` | No | URL JDBC; por defecto `jdbc:postgresql://localhost:5432/tecnouam-db`. |
| `DB_USER` | No | Usuario PostgreSQL; por defecto `postgres`. |
| `API_PROXY_TARGET` | No | Destino del proxy de Vite; por defecto `http://localhost:8080`. |

Para generar una clave JWT nueva puede usar `openssl rand -hex 32`. No coloque contraseñas ni tokens en archivos versionados.

## Ejecutar

1. Confirme que PostgreSQL esté iniciado y publique el puerto configurado en `DB_URL`.
2. Desde `api`, ejecute `./mvnw spring-boot:run`. La API queda en `http://localhost:8080`. Si falta la base y el usuario tiene permiso `CREATEDB`, la aplicación la crea; al arrancar crea las tablas `equipos` y `prestamos` con sus restricciones. También puede ejecutar [los scripts de base de datos](api/db/) manualmente.
3. Desde `client`, ejecute `npm ci` y luego `npm run dev`. Abra la URL que indique Vite, normalmente `http://localhost:5173`.
4. Inicie sesión en React con una cuenta de prueba. La tabla consulta `GET /api/equipos` con el JWT. Al recargar o pulsar **Actualizar lista**, refleja altas y préstamos registrados desde las solicitudes `.http`.

## Cuentas de prueba

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | `ADMIN` | Valor de `ADMIN_PASSWORD` en `api/.env`. |
| `user` | `USER` | Valor de `USER_PASSWORD` en `api/.env`. |

`USER` puede consultar equipos y préstamos. `ADMIN` también puede crear, actualizar y eliminar equipos, registrar préstamos y devoluciones.

## Endpoints

La autenticación usa `Authorization: Bearer <token>`. `POST /api/auth/login` acepta `{"username":"admin","password":"<contraseña>"}` y devuelve `token`, `tokenType` y `expiresIn`.

| Método | Ruta | Rol | Éxito |
|---|---|---|---|
| POST | `/api/auth/login` | Público | 200 |
| GET | `/api/equipos` | USER, ADMIN | 200 |
| GET | `/api/equipos/{id}` | USER, ADMIN | 200 |
| POST | `/api/equipos` | ADMIN | 201 |
| PUT | `/api/equipos/{id}` | ADMIN | 200 |
| DELETE | `/api/equipos/{id}` | ADMIN | 204 |
| GET | `/api/prestamos` | USER, ADMIN | 200 |
| POST | `/api/prestamos` | ADMIN | 201 |
| PUT | `/api/prestamos/{id}/devolver` | ADMIN | 200 |

Ejemplo de equipo: `{"codigoPatrimonial":"PAT-001","nombre":"Portátil","tipo":"Computadora"}`. Ejemplo de préstamo: `{"equipoId":1,"nombreSolicitante":"Ana López","fechaPrestamo":"2026-10-28","fechaPrevistaDevolucion":"2026-11-04"}`. Las fechas usan `AAAA-MM-DD`.

La API devuelve `400` para datos inválidos, `401` sin autenticación o con credenciales incorrectas, `403` si el rol no tiene permiso, `404` si falta el recurso y `409` ante conflictos de negocio.

## Solicitudes manuales

Abra estos archivos con REST Client de VS Code y ejecute las solicitudes en orden dentro de cada uno. Leen las contraseñas desde `api/.env` y encadenan los JWT e identificadores de sus propias respuestas:

- [CRUD de equipos](api/01-equipos-crud.http)
- [Préstamos](api/02-prestamos.http)
- [Devoluciones](api/03-devoluciones.http)
- [Seguridad](api/04-seguridad.http)

Consulte [detalles de la API](api/README.md) y [detalles del cliente](client/README.md).
