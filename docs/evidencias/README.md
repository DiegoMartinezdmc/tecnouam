# Evidencias verificadas

`resultados-api.json` contiene la ejecución de las 22 solicitudes de la colección Postman mediante Newman contra el servidor HTTP real, sin credenciales ni tokens.

Las capturas `react-user.png`, `react-admin-prestado.png` y `react-devolucion.png` corresponden a pruebas de navegador con la API y la base relacional reales. Los registros temporales creados por esas pruebas se eliminan al finalizar.

`react-carga.png`, `react-vacio.png` y `react-error.png` documentan estados visuales reproducidos mediante respuestas controladas en el navegador de pruebas; no se usan como evidencia de datos reales.

Para repetirlas, sigue `../PRUEBAS.md`. El informe navegable de los cuatro escenarios se genera en `frontend/playwright-report/index.html` y se excluye del repositorio porque es un archivo de salida regenerable. Las capturas de la aplicación Postman y la defensa de los cinco integrantes se realizan según el guion de entrega.
