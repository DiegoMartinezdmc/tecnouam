# Pruebas y defensa técnica

## Evidencias que debe guardar el equipo

Ejecutar el backend y React. Importar la colección Postman y configurar contraseñas localmente. Capturar cada respuesta con URL, método, código HTTP y cuerpo visibles; ocultar tokens y contraseñas.

1. Login correcto y login rechazado con 401.
2. GET sin token: 401. GET con USER: 200. POST con USER: 403.
3. Alta de equipo con ADMIN: 201 y Location. Consulta: 200 y datos guardados.
4. Actualización: 200 y cambio visible en React al pulsar Actualizar.
5. Préstamo: 201; equipo PRESTADO. Segundo préstamo: 409.
6. Eliminación durante el préstamo: 409.
7. Devolución: 200; equipo DISPONIBLE. Devolución repetida: 409.
8. Eliminación después de devolver: 204. Consulta posterior: 404.
9. Fechas invertidas y campos vacíos: 400. Código repetido: 409.
10. React como USER: tablas de equipos y préstamos con consulta real. React como ADMIN: formularios y acciones habilitadas.
11. Estado vacío: buscar un código inexistente. Para demostrar lista vacía real, usar una base de prueba sin equipos. Para error de red, detener el backend y pulsar Actualizar; reiniciarlo y recuperar la consulta. La carga se observa mientras se espera la respuesta.
12. Reiniciar el backend y verificar que permanece un equipo creado que no haya sido eliminado.

Guardar las capturas propias en `docs/evidencias/` y agregar una breve descripción de qué verifica cada imagen. El informe de pruebas automatizadas se produce en `backend/target/surefire-reports/`. Las capturas Postman y la defensa no se sustituyen por ese informe.

## Defensa de cinco integrantes

| Integrante | Tema | Verificación en vivo |
| --- | --- | --- |
| 1 | Equipo, Prestamo, relación ManyToOne y migraciones | Explicar FK y persistencia tras reiniciar |
| 2 | Controlador, DTO, servicio y transacciones | Crear equipo y rechazar código duplicado |
| 3 | BCrypt, JWT, firma, vencimiento y roles | Contrastar 401 y 403 |
| 4 | React, useState, useEffect y Axios | Actualizar React después de crear desde Postman |
| 5 | Casos funcionales y pruebas | Préstamo doble, devolución y eliminación |

Cada integrante debe comprender el flujo completo: Axios envía Bearer, Spring Security valida el JWT y autoriza el rol, el controlador valida la entrada, el servicio aplica reglas en una transacción, JPA guarda en H2 y la respuesta actualiza la interfaz.

401 indica que falta una identidad autenticada válida. 403 indica que la identidad autenticada no posee el permiso requerido. El JWT está firmado, no cifrado: no se incluyen contraseñas en su contenido.

## Repositorio compartido

Crear el repositorio del equipo y conectar esta carpeta. Cada estudiante debe configurar su nombre y correo, realizar cambios verificables en su parte y registrar sus propios commits. No fabricar autores o contribuciones. Sustituir las responsabilidades orientativas por los nombres reales en la entrega.
