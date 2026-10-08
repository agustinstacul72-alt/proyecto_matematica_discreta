<div align="center">
  <img src="src/main/resources/com/medset/medsetai/images/medset-logo.png" alt="Logotipo de MedSet AI" width="140">

  <h1>MedSet AI</h1>
  <p><strong>Matemática discreta · Grupo 6</strong></p>
  <p>Una aplicación educativa para explorar teoría de conjuntos e inteligencia artificial mediante el análisis simulado de síntomas.</p>

  <p>
    <img src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white" alt="Java 25">
    <img src="https://img.shields.io/badge/JavaFX-21-1B6AC6" alt="JavaFX 21">
    <img src="https://img.shields.io/badge/License-MIT-2563EB" alt="Licencia MIT">
  </p>

  <p><strong>Proyecto educativo · No es una herramienta médica ni ofrece diagnósticos.</strong></p>
</div>

---

Aplicación de escritorio educativa desarrollada con JavaFX para explorar el
análisis simulado de síntomas mediante teoría de conjuntos e inteligencia
artificial.

## Equipo

Proyecto de Matemática Discreta · Grupo 6

- Daniel Tueros Cueva
- Brisa Abigail Alvarez
- Stacul Dario Agustin
- Dayana Lucia Posada Narváez
- Agustin Tomas Gammuto
- Jose David Carranza Angarita

El repositorio original contenía únicamente un `Main.java` vacío y un README
inicial. Esta rama incorpora la aplicación JavaFX existente, su configuración
Maven, recursos, documentación y una plantilla segura de configuración local.

## Estado actual

- Pantalla de bienvenida y pantalla informativa del proyecto.
- Dashboard con chat conectado a los modelos de Ollama y un formulario de
  paciente en desarrollo.
- Dos modelos locales de Ollama: Gemma 3 4B y Nemotron 3 Nano 4B (predeterminado).
- Configuración de los modelos locales mediante archivo `.env`.
- Los modelos `Patient`, `Symptom`, `Pattern` y `AnalysisResult` son
  actualmente marcadores vacíos.
- La generación de informes de paciente y gráficos aún son acciones
  provisionales; consulta [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

## Versiones y requisitos

| Componente | Versión para esta release | ¿Se incluye en el instalador? |
| --- | --- | --- |
| MedSet AI | `1.0.0` | Sí |
| Java | 25 | Sí, como runtime privado de la aplicación |
| JavaFX | 21.0.6 | Sí, junto con la aplicación |
| Ollama | `0.40.1` fue la versión probada durante el desarrollo | No |
| Gemma | `gemma3:4b` | No; se descarga en Ollama |
| Nemotron | `nemotron-3-nano:4b` | No; se descarga en Ollama |

Para **usar el chat de IA**, instala Ollama y descarga al menos uno de los dos
modelos. La aplicación y sus instaladores no instalan Ollama ni descargan
modelos automáticamente: son componentes separados y los modelos ocupan varios
GB. La aplicación puede abrirse sin Ollama, pero el chat no podrá responder
hasta que Ollama esté instalado, ejecutándose y tenga disponible el modelo
seleccionado.

El instalador nativo no requiere que instales Java ni Maven. JDK 25 y conexión
a internet para descargar dependencias solo hacen falta si vas a compilar o
ejecutar el proyecto desde el código fuente.

Las versiones `gemma3:4b` y `nemotron-3-nano:4b` son etiquetas de modelo de
Ollama; Ollama puede actualizar los archivos asociados a esas etiquetas al
descargarlos. No se fija aquí un hash inmutable del modelo.

## Ejecutar

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd javafx:run
```

En macOS o Linux:

```bash
bash ./mvnw javafx:run
```

## Descargar e instalar la aplicación

Cuando haya una release publicada, abre
[Releases](https://github.com/agustinstacul72-alt/proyecto_matematica_discreta/releases/latest)
y descarga el paquete de tu sistema: `.msi` para Windows, `.deb` para Ubuntu o
`.pkg` para macOS. Los paquetes son nativos de su sistema y no se pueden
intercambiar entre Windows, Linux y macOS. Sigue el asistente del sistema para
instalar MedSet AI.

### Preparar Ollama para el chat

1. Instala Ollama desde la [página oficial de descarga](https://ollama.com/download)
   y abre/inicia Ollama para que el servicio local quede activo. En Linux, sigue
   las instrucciones oficiales de instalación para tu distribución.
2. Abre una terminal y descarga al menos el modelo que vayas a usar:

   ```text
   ollama pull nemotron-3-nano:4b
   ollama pull gemma3:4b
   ```

   El segundo comando es opcional si solo quieres utilizar Nemotron. Cada modelo
   se descarga por separado y ocupa espacio en disco; la descarga necesita
   conexión a internet, pero las consultas posteriores se ejecutan localmente.
3. Comprueba la instalación con `ollama --version` y
   `ollama list`. Después abre MedSet AI y selecciona un modelo que aparezca en
   esa lista.

Durante el desarrollo se comprobó el uso de Ollama `0.40.1` con
`nemotron-3-nano:4b`. La aplicación se conecta al servidor local en
`http://localhost:11434` por defecto. Si Ollama usa otra dirección, configura
`OLLAMA_URL` en el archivo `.env` del directorio desde el que se inicia la
aplicación o en el entorno del sistema.

## Crear instaladores de una release

Al crear y subir un tag de versión (`v1.0.0`, por ejemplo), GitHub Actions
ejecuta las pruebas, genera un instalador nativo y comprueba que la aplicación
instalada arranque en Windows, Linux y macOS. Si todos los trabajos terminan
correctamente, el workflow crea una GitHub Release con los instaladores `.msi`,
`.deb` y `.pkg` adjuntos. También puedes iniciar el workflow manualmente para
probar la creación e instalación sin publicar una release.

Para publicar una nueva versión desde la terminal:

```bash
git switch main
git pull
git tag v1.0.0
git push origin v1.0.0
```

Los instaladores incluyen Java 25 y JavaFX 21.0.6; no requieren que el usuario
instale un JDK. **Ollama y al menos un modelo local deben instalarse por
separado**. El workflow comprueba la instalación del paquete y el arranque de
las vistas, pero no instala Ollama ni descarga modelos durante la instalación.

Antes de publicar, confirma que el workflow **Build release installers** acabó
con éxito en los tres sistemas operativos. No reutilices un tag de versión ya
publicado.

La aplicación se inicia desde
`com.medset.medsetai.MedSetApplication`. `Main.java` se conserva como un
lanzador de compatibilidad para quienes ejecuten la clase `Main` desde un IDE.

## Configuración de Ollama desde el código fuente

1. Copia `.env.example` a `.env` en la raíz del proyecto.
2. Instala e inicia Ollama y descarga uno o ambos modelos:

   ```text
   ollama pull gemma3:4b
   ollama pull nemotron-3-nano:4b
   ```

3. Ajusta opcionalmente la dirección y los nombres de los modelos en `.env`.

| Variable | Uso | Valor predeterminado |
| --- | --- | --- |
| `OLLAMA_URL` | Dirección base del servidor Ollama | `http://localhost:11434` |
| `OLLAMA_MODEL` | Primer modelo local | `gemma3:4b` |
| `OLLAMA_NEMOTRON_MODEL` | Segundo modelo local | `nemotron-3-nano:4b` |

Ambas opciones se ejecutan a través del servidor Ollama local; no requieren una
clave de API ni activar facturación de una API en la nube. Cada modelo debe
descargarse antes de seleccionarlo. La primera consulta puede tardar más si
Ollama todavía lo está cargando.

Ambos modelos transmiten las respuestas mientras se generan para que el
texto empiece a aparecer antes de que termine la respuesta completa. Ollama
desactiva el razonamiento visible de Nemotron para que los 512 tokens de salida
se usen en la respuesta y conserva cada modelo cargado durante diez minutos.

## Verificación

Ejecuta las pruebas y la compilación con:

```powershell
.\mvnw.cmd test
```

```bash
bash ./mvnw test
```

## Estructura

```text
.
├── .github/
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/maven.yml
├── docs/
│   └── ARQUITECTURA.md
├── src/main/java/com/medset/medsetai/
│   ├── Launcher.java
│   ├── MedSetApplication.java
│   ├── controller/
│   ├── model/
│   ├── service/
│   └── util/
└── src/main/resources/com/medset/medsetai/
    ├── css/
    ├── images/
    └── view/
```

Para detalles de clases, vistas, servicios y pendientes de implementación,
consulta la [guía de arquitectura](docs/ARQUITECTURA.md).

## Flujo de contribución

Trabaja en una rama separada, por ejemplo `dev-jose`, y abre un pull request
dirigido a `main`. No subas directamente a `main`. Antes de solicitar revisión,
ejecuta `mvnw test` y describe los cambios y las pruebas realizadas en el pull
request.

## Licencia

El código fuente y la documentación se distribuyen bajo la licencia MIT;
consulta [`LICENSE`](LICENSE). Los logotipos e imágenes del proyecto quedan
excluidos y mantienen los derechos de sus respectivos propietarios.

<div align="center">
  <sub>Proyecto académico de Matemática Discreta · Grupo 6</sub><br>
  <img src="src/main/resources/com/medset/medsetai/images/jala-logo.png" alt="Jala University" width="180">
</div>
