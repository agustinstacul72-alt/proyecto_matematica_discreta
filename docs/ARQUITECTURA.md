# Arquitectura y guía del proyecto

## Propósito y límites

MedSet AI es una aplicación de escritorio educativa para practicar ideas de
teoría de conjuntos con información de pacientes y síntomas simulados. JavaFX
construye la interfaz; Ollama ejecuta modelos de lenguaje en el equipo; SQLite
guarda los registros localmente.

La aplicación no es una herramienta médica. El servicio que normaliza síntomas
solo transforma texto en etiquetas comparables; no diagnostica enfermedades,
estima riesgos ni recomienda tratamientos. La selección de pacientes se puede
preparar para un análisis, pero todavía no existen las operaciones de
conjuntos, el informe ni el gráfico.

## Tecnologías y versiones

| Componente | Versión o configuración |
| --- | --- |
| Java | JDK 25 |
| JavaFX | 21.0.6 (FXML y controles) |
| Maven | Wrapper incluido en el repositorio |
| Ollama | Servidor local compatible con `http://localhost:11434` |
| Modelos | `gemma3:4b` y `nemotron-3-nano:4b` |
| Persistencia | SQLite con `sqlite-jdbc` 3.50.3.0 |
| JSON | Jackson 2.20.0 |
| Configuración | dotenv-java 3.2.0 y `.env` opcional |
| Pruebas | JUnit 5 |

La release empaqueta el runtime de Java y las dependencias de JavaFX. Ollama y
los modelos son dependencias externas y se instalan aparte.

## Estructura de paquetes

```text
src/main/java/com/medset/medsetai/
├── Launcher.java
├── MedSetApplication.java
├── controller/
│   ├── WelcomeController.java
│   ├── CreditsController.java
│   ├── DashboardController.java
│   └── PatientController.java
├── model/
│   ├── Patient.java
│   ├── Symptom.java
│   ├── AnalysisResult.java
│   └── Pattern.java
├── repository/
│   ├── DatabaseManager.java
│   └── PatientRepository.java
├── service/
│   ├── AiProvider.java
│   ├── AiService.java
│   ├── OllamaProvider.java
│   ├── PatientService.java
│   ├── PatientSelectionContext.java
│   └── SymptomNormalizer.java
└── util/
    ├── AppConfig.java
    ├── SceneManager.java
    ├── ChatMarkdownFormatter.java
    └── ChatMarkdownRenderer.java
```

Las vistas FXML y la hoja de estilos se encuentran en
`src/main/resources/com/medset/medsetai/`. Las pruebas están en `src/test/java`.

## Arranque y navegación

1. `Launcher.main` inicia JavaFX y delega en `MedSetApplication`.
2. `MedSetApplication.start` inicializa las tablas locales de SQLite y carga
   `welcome-view.fxml`.
3. Cada FXML declara su controlador con `fx:controller`. Los controladores
   responden a los botones y a los eventos de las vistas.
4. `SceneManager.switchTo` carga otra vista en la ventana existente; no crea
   otra instancia de la aplicación.
5. El argumento `--smoke-test` carga las vistas de bienvenida, dashboard,
   créditos y pacientes, muestra una ventana breve y cierra el proceso. Sirve
   para verificar recursos y controladores sin enviar peticiones a Ollama.

| Vista | Controlador | Responsabilidad |
| --- | --- | --- |
| `welcome-view.fxml` | `WelcomeController` | Inicio y acceso al dashboard o a créditos. |
| `dashboard-view.fxml` | `DashboardController` | Chat de Ollama y acceso a pacientes. |
| `credits-view.fxml` | `CreditsController` | Información del curso y navegación de regreso. |
| `patient-view.fxml` | `PatientController` | Alta, edición, eliminación y selección de pacientes. |

`Main.java` es un lanzador de compatibilidad en la raíz del proyecto.

## Chat de IA y formato de respuestas

El recorrido de una pregunta es:

1. `DashboardController` lee el texto y el modelo seleccionado.
2. `AiService` delega la petición en `OllamaProvider`.
3. `OllamaProvider` envía JSON por HTTP a `{OLLAMA_URL}/api/chat`. El cuerpo
   solicita transmisión NDJSON, salida en streaming, hasta 512 tokens, y
   mantiene el modelo cargado durante diez minutos. `think: false` evita que
   Nemotron devuelva razonamiento en lugar de la respuesta.
4. Cada fragmento regresa al hilo JavaFX mediante `Platform.runLater`; las
   llamadas de red no bloquean la interfaz.
5. `ChatMarkdownFormatter` interpreta párrafos, títulos, listas, énfasis,
   código y comandos matemáticos comunes. `ChatMarkdownRenderer` crea nodos de
   texto JavaFX con estilos CSS; no interpreta HTML del modelo.

El prompt del chat pide contestar en el idioma de la pregunta, usar Markdown
para enfatizar y preferir símbolos matemáticos Unicode. El formateador también
interpreta negrita (`**texto**`), cursiva (`*texto*`), código con acentos
graves, encabezados `#`, listas numeradas y con viñetas, citas y bloques de
código. Convierte comandos LaTeX frecuentes como `\cup`, `\cap`, `\in`,
`\subseteq`, `\emptyset`, `\mathbb{R}`, `\frac{a}{b}` y `x^2` en notación
legible. No implementa un motor tipográfico completo de LaTeX.

Un error de conexión, tiempo de espera, modelo ausente o respuesta vacía se
muestra junto al mensaje; no se oculta como una respuesta satisfactoria.

## Gestión de pacientes, normalización y SQLite

El flujo de pacientes está separado por responsabilidades:

1. `PatientController` obtiene el formulario, valida que la edad sea numérica
   y coordina alta, edición, eliminación y selección múltiple. Las peticiones
   que llaman a IA corren en `CompletableFuture`; las actualizaciones de UI
   regresan al hilo JavaFX.
2. `PatientService` valida nombre, edad (0–130) y síntomas, solicita la
   normalización y delega la persistencia.
3. `SymptomNormalizer` envía a Nemotron un prompt para devolver JSON con el
   idioma detectado y etiquetas normalizadas en inglés (`snake_case`), sin
   inferir síntomas ausentes ni diagnosticar. El resultado se valida y se
   eliminan duplicados conservando el orden.
4. `PatientRepository` crea y actualiza al paciente y sus síntomas en una
   transacción. Usa `PreparedStatement`, recupera el ID de SQLite y consulta,
   lista o elimina los registros.
5. `DatabaseManager` crea y configura la conexión JDBC, habilita claves foráneas
   y crea las tablas `patients` y `symptoms`. La eliminación en cascada quita
   los síntomas asociados.

La base de datos se guarda en:

```text
<carpeta del usuario>/.medset-ai/medset.db
```

En Windows, por ejemplo, suele ser `C:\Users\<usuario>\.medset-ai\medset.db`.
Los registros se guardan localmente. Al crear o actualizar un paciente, el
texto de síntomas se envía al servidor Ollama local para normalizar etiquetas;
los demás datos del paciente no forman parte de esa solicitud.

`PatientSelectionContext` mantiene en memoria una copia de los pacientes
seleccionados para un futuro módulo de análisis; esa selección no reemplaza ni
modifica los registros persistidos.

## Modelos del dominio

| Modelo | Datos/uso |
| --- | --- |
| `Patient` | ID local, nombre, edad y síntomas asociados. |
| `Symptom` | Texto original, nombre normalizado, idioma y relaciones por ID. |
| `Pattern` | Estructura inicial para patrones educativos; aún sin operaciones. |
| `AnalysisResult` | Estructura inicial para resultados e informes futuros. |

Los dos últimos modelos todavía no implementan algoritmos de conjuntos ni
reglas de análisis.

## Configuración y conexión con Ollama

`AppConfig` carga `.env` si existe; también funciona sin el archivo usando estos
valores predeterminados:

| Variable | Uso | Predeterminado |
| --- | --- | --- |
| `OLLAMA_URL` | URL del servidor Ollama | `http://localhost:11434` |
| `OLLAMA_MODEL` | Modelo Gemma | `gemma3:4b` |
| `OLLAMA_NEMOTRON_MODEL` | Modelo Nemotron | `nemotron-3-nano:4b` |

El servidor local debe estar ejecutándose y el modelo seleccionado debe estar
descargado. Para descargarlo desde una terminal:

```text
ollama pull nemotron-3-nano:4b
ollama pull gemma3:4b
```

No hacen falta claves de API en la nube. Si el servidor usa otra dirección,
configura `OLLAMA_URL`; la llamada de chat usa el endpoint `/api/chat`.

## Pruebas, ejecución y empaquetado

En Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd javafx:run
.\mvnw.cmd javafx:run '-Djavafx.args=--smoke-test'
```

En macOS o Linux:

```bash
bash ./mvnw test
bash ./mvnw javafx:run
bash ./mvnw javafx:run '-Djavafx.args=--smoke-test'
```

Las pruebas unitarias comprueban el servicio de IA, el protocolo de Ollama y el
formateo de Markdown/notación matemática. GitHub Actions ejecuta las pruebas,
crea e instala los paquetes nativos de Windows, Ubuntu y macOS y ejecuta el
smoke test. Los instaladores no contienen Ollama ni los modelos.

## Dependencias principales

`pom.xml` declara JavaFX Controls/FXML 21.0.6, ControlsFX, FormsFX, Ikonli,
BootstrapFX, Jackson, dotenv-java, SQLite JDBC y JUnit 5. El Maven Wrapper
permite compilar sin instalar Maven globalmente. `javafx.version` centraliza la
versión de JavaFX compartida por Controls y FXML.

## Flujo Git

- `main` es la rama de integración.
- Desarrolla en una rama de trabajo, por ejemplo `dev-jose`.
- Abre un Pull Request hacia `main` y revisa los checks de GitHub Actions.
- Las releases se publican con tags `vX.Y.Z`; Actions crea adjuntos nativos
  solo si pasan las pruebas de empaquetado e instalación de todas las
  plataformas.
