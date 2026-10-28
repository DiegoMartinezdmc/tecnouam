# Cliente TecnoUAM

Interfaz React para consultar los equipos reales de la API. Usa Axios y un JWT emitido por `POST /api/auth/login`. La tabla muestra código patrimonial, nombre, tipo y estado; también presenta carga, inventario vacío y error de conexión. No contiene datos simulados.

## Ejecutar

1. Inicie la API desde `api` con `ADMIN_PASSWORD`, `USER_PASSWORD` y `JWT_SECRET` definidos. Consulte [la guía de la API](../api/README.md).
2. En `client`, ejecute `npm install` y `npm run dev`.
3. Abra la URL que muestra Vite e inicie sesión como `user` o `admin` usando la contraseña configurada en la API.

Vite envía `/api` a `http://localhost:8080` durante el desarrollo. Si la API usa otro puerto, ejecute Vite con `API_PROXY_TARGET=http://localhost:PUERTO npm run dev`. Para servir la compilación de producción, configure el servidor web para reenviar `/api` al backend.

El token se conserva en `sessionStorage` para que una recarga de la pestaña vuelva a consultar `GET /api/equipos`. El botón **Actualizar lista** hace la misma consulta. Si un administrador crea un equipo o registra un préstamo con los archivos `.http` de `api`, la siguiente recarga o actualización muestra el equipo nuevo o su estado `PRESTADO`. Si el token vence, se solicita iniciar sesión otra vez.

Axios está incluido como dependencia local en `vendor/axios` porque el entorno de desarrollo no tiene acceso al registro npm. Se conserva su licencia en esa carpeta.
