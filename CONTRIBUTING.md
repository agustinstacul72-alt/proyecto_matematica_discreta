# Contribuir

1. Actualiza tu copia de `main` y crea una rama de trabajo descriptiva, por
   ejemplo `dev-jose` o `feature/nombre-del-cambio`.
2. Implementa un cambio acotado y documenta las clases, configuración o
   comportamiento que hayas añadido.
3. Ejecuta `.\mvnw.cmd test` en Windows o `bash ./mvnw test` en macOS/Linux.
4. Abre un pull request hacia `main` usando la plantilla del repositorio.
5. Responde a la revisión y espera que pasen las comprobaciones antes de
   integrar los cambios.

No incluyas `.env`, claves de API, datos de pacientes reales, directorios
generados (`target/`) ni archivos privados del IDE. Usa únicamente datos
simulados: este proyecto es educativo y no es un dispositivo médico.
