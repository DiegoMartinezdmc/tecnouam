# Ejecución y pruebas del examen

Todos los comandos siguientes se ejecutan desde la carpeta raíz del proyecto, en la terminal PowerShell de Visual Studio Code.

## 1. Iniciar la aplicación

Terminal 1, backend:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-backend.ps1
```

Esperar el mensaje `Started Application`. La API escucha en http://localhost:8080. La base H2 se inicia con el backend y se guarda en `backend/data/`; no hay que abrir otro programa de base de datos.

Terminal 2, frontend:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/start-frontend.ps1
```

Abrir http://localhost:5173. Usuario `admin`, contraseña del campo `ADMIN_PASSWORD` del archivo `.env` de la raíz. Para consultar sin modificar, usar `user` y `USER_PASSWORD`. No copiar las comillas ni el nombre de la variable. El archivo `.env` se genera automáticamente en el primer arranque.

Mantener ambas terminales abiertas. No iniciar dos veces el backend: H2 bloquea el archivo mientras está abierto. Si el puerto está ocupado, volver a la terminal donde se ejecuta el servicio y detenerlo con Ctrl+C antes de reiniciar. La recarga completa del navegador cierra la sesión porque el token permanece en memoria; el botón Actualizar conserva la sesión.

## 2. Ejecutar las pruebas automatizadas

Terminal 3:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/test.ps1 -Api
```

Este comando ejecuta la prueba de integración Java sobre una base temporal, compila React y ejecuta la colección Postman por HTTP con Newman contra el backend iniciado. Instala Newman localmente si hace falta. La prueba Java no modifica la base de demostración. La colección HTTP crea un equipo de prueba y lo elimina después de devolverlo.

Resultados esperados: `BUILD SUCCESS`, compilación Vite correcta, `Solicitudes: 22. Fallos: 0` y `Verificacion completada correctamente`. El informe HTTP sin tokens ni contraseñas queda en `docs/evidencias/resultados-api.json`; el informe Java en `backend/target/surefire-reports/`.

Para probar Java y compilar React sin iniciar servidores:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/test.ps1
```

Para probar React en un navegador automatizado, con ambos servidores iniciados:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/test-ui.ps1
```

La primera vez se descarga Chromium dentro de `.tools/browsers`. Los cuatro escenarios verifican consulta USER sin botones de escritura, alta/edición/préstamo/devolución ADMIN, actualización después de un alta externa y estados de carga/vacío/error. Guardan capturas PNG reales en `docs/evidencias/` y un informe HTML en `frontend/playwright-report/index.html`. Los tres primeros escenarios consumen la API y la base reales; solamente el último controla respuestas del navegador para reproducir estados visuales sin vaciar la base ni interrumpir el servidor.

## 3. Demostrar en Postman

1. Abrir Postman y seleccionar **Import > Files**.
2. Importar `postman/TecnoUAM.postman_collection.json`.
3. Abrir la colección y su pestaña **Variables**.
4. Dejar `baseUrl` como `http://localhost:8080`.
5. Completar los valores locales de `adminPassword` y `userPassword` con las claves de `.env`. No compartir esos valores.
6. Pulsar **Run collection**, seleccionar las 22 solicitudes y ejecutar una iteración en orden. No ejecutarlas en paralelo: los identificadores se guardan al crear cada registro.
7. Verificar que todas las pruebas aparecen en verde. Para demostrar cambios en React, ejecutar individualmente hasta la solicitud 06 o 12, pulsar Actualizar en React y tomar la captura antes de continuar hasta eliminar el registro.

Login guarda automáticamente `adminToken` y `userToken`. El alta guarda `equipoId`; el préstamo guarda `prestamoId`. No se necesita copiar tokens manualmente. Un token vence después de una hora: repetir los dos login si es necesario.

| Número | Prueba | Resultado esperado |
| --- | --- | --- |
| 01 | Login ADMIN | 200, JWT firmado |
| 02 | Login USER | 200, JWT firmado |
| 03 | GET equipos sin token | 401 |
| 04 | Login incorrecto | 401 |
| 05 | USER intenta crear equipo | 403 |
| 06 | ADMIN crea equipo | 201, ID y Location |
| 07 | Repetir código patrimonial | 409 |
| 08 | USER consulta equipos | 200, lista real |
| 09 | Consulta del equipo por ID | 200 |
| 10 | Actualizar nombre | 200, nombre nuevo |
| 11 | Devolución prevista anterior al préstamo | 400 |
| 12 | Registrar préstamo | 201 |
| 13 | Consultar equipo prestado | 200, estado PRESTADO |
| 14 | Prestar el mismo equipo otra vez | 409 |
| 15 | Eliminar equipo con préstamo activo | 409 |
| 16 | USER consulta préstamos | 200 |
| 17 | Devolver préstamo | 200 |
| 18 | Repetir devolución | 409 |
| 19 | Consultar después de devolver | 200, estado DISPONIBLE |
| 20 | Eliminar equipo sin préstamo activo | 204, sin cuerpo |
| 21 | Consultar equipo eliminado | 404 |
| 22 | Enviar campos obligatorios vacíos | 400 |

Si prefieres crear solicitudes manualmente, seleccionar **Body > raw > JSON** para POST y PUT de equipos. En **Authorization > Bearer Token**, introducir el token del rol adecuado.

Alta o actualización de equipo:

```json
{"codigoPatrimonial":"DEMO-100","nombre":"Portátil del laboratorio","tipo":"PORTATIL"}
```

Préstamo, sustituyendo `1` por el ID devuelto en el alta:

```json
{"equipoId":1,"solicitante":"Instructor de demostración","fechaPrestamo":"2026-10-26","fechaPrevistaDevolucion":"2026-10-28"}
```

Devolución: `PUT /api/prestamos/{prestamoId}/devolver`, sin cuerpo. El estado de un equipo se modifica mediante préstamos y devoluciones; el PUT de equipo solo cambia código, nombre y tipo.

## 4. Verificar React y guardar evidencias

Con ADMIN, crear un equipo, editarlo, prestarlo y devolverlo desde las tablas. Con USER, comprobar que solo aparecen consultas. Un cambio creado desde Postman debe aparecer al pulsar Actualizar en React.

Guardar capturas de Postman donde se vean método, URL, estado HTTP y respuesta. Incluir alta, consulta, actualización, préstamo, rechazo de segundo préstamo, eliminación bloqueada, devolución, estado disponible, eliminación permitida, 401 y 403. No incluir la pantalla del login con contraseñas ni encabezados con tokens. El JSON de Newman complementa estas capturas; no es una captura de la aplicación Postman.

Las capturas automatizadas `react-user.png`, `react-admin-prestado.png` y `react-devolucion.png` muestran integración real. Las de carga, vacío y error muestran escenarios visuales controlados y deben presentarse como tales.

Para verificar persistencia, crear un equipo que no sea el temporal de las pruebas, detener el backend con Ctrl+C, iniciarlo de nuevo, volver a iniciar sesión y verificar que el equipo sigue visible. No borrar `backend/data/`.

## 5. Defensa y entrega

El código, migraciones, pruebas y colección están en esta carpeta. La explicación y distribución de temas están en `DEMOSTRACION.md`. Los cinco integrantes deben realizar sus propios aportes y commits, conocer el sistema completo y participar en la defensa. La ejecución automatizada no sustituye esa participación.
