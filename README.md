# MedSet AI

Aplicación de escritorio educativa desarrollada con JavaFX para explorar el
análisis simulado de síntomas mediante teoría de conjuntos e inteligencia
artificial. **No es una herramienta médica ni ofrece diagnósticos.**

El repositorio original contenía únicamente un `Main.java` vacío y un README
inicial. Esta rama incorpora la aplicación JavaFX existente, su configuración
Maven, recursos, documentación y una plantilla segura de configuración local.

## Estado actual

- Pantalla de bienvenida y pantalla informativa del proyecto.
- Dashboard con chat conectado a los proveedores de IA y un formulario de
  paciente en desarrollo.
- Abstracción de proveedores de IA, con implementaciones para Gemini y Ollama.
- Configuración local de proveedores mediante archivo `.env`.
- Los modelos `Patient`, `Symptom`, `Pattern` y `AnalysisResult` son
  actualmente marcadores vacíos.
- La generación de informes de paciente y gráficos aún son acciones
  provisionales; consulta [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

## Requisitos

- JDK 25.
- Maven Wrapper incluido en el repositorio (no hace falta instalar Maven).
- Conexión a internet para descargar dependencias la primera vez.
- Para usar Gemini: una clave de API.
- Para usar Ollama: un servidor Ollama local o accesible y el modelo configurado
  descargado en ese servidor.

## Ejecutar

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd javafx:run
```

En macOS o Linux:

```bash
bash ./mvnw javafx:run
```

La aplicación se inicia desde
`com.medset.medsetai.MedSetApplication`. `Main.java` se conserva como un
lanzador de compatibilidad para quienes ejecuten la clase `Main` desde un IDE.

## Configuración de IA

1. Copia `.env.example` a `.env` en la raíz del proyecto.
2. Si usarás Gemini, reemplaza `GEMINI_API_KEY` con tu clave. No la compartas ni
   la subas al repositorio.
3. Ajusta opcionalmente los nombres de modelo y la URL de Ollama.

| Variable | Uso | Valor predeterminado |
| --- | --- | --- |
| `GEMINI_API_KEY` | Clave de Gemini; requerida al crear `GeminiProvider` | Sin valor |
| `GEMINI_MODEL` | Identificador del modelo Gemini | `gemini-3.8-flash` |
| `OLLAMA_URL` | Dirección base del servidor Ollama | `http://localhost:11434` |
| `OLLAMA_MODEL` | Modelo disponible en Ollama | `gemma3:4b` |

La configuración actual se lee del archivo `.env`; no agregues credenciales a
los archivos Java, FXML, README o commits.

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
