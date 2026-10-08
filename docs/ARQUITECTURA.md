# Arquitectura y referencia del código

## Resumen

MedSet AI es una aplicación Java de escritorio con JavaFX y vistas FXML. La
clase `MedSetApplication` crea la ventana inicial; los controladores gestionan
la navegación; `SceneManager` sustituye las escenas; y los servicios aíslan la
comunicación con proveedores de IA.

El proyecto se encuentra en desarrollo. Esta guía describe lo que hace el
código actual y señala las partes que todavía son estructuras iniciales, no
funcionalidad terminada.

## Inicio y navegación

| Archivo | Responsabilidad |
| --- | --- |
| `Launcher.java` | Punto de entrada auxiliar que inicia `MedSetApplication` con `Application.launch`. |
| `MedSetApplication.java` | Carga `welcome-view.fxml`, crea la escena inicial y configura el título y tamaño mínimo de la ventana. |
| `util/SceneManager.java` | Carga una vista desde `resources/.../view`, crea una escena de 1200 × 750 y la asigna a la ventana existente. |
| `controller/WelcomeController.java` | Cambia desde la bienvenida al dashboard o a la pantalla de créditos. |
| `controller/CreditsController.java` | Regresa desde la pantalla informativa a la bienvenida. |
| `controller/DashboardController.java` | Gestiona la navegación entre paneles, el chat asíncrono y las acciones provisionales del formulario de paciente. |

`Main.java` (en la raíz) es un lanzador de compatibilidad para delegar en
`Launcher`; el punto de entrada configurado para JavaFX en Maven es
`MedSetApplication`.

## Vistas y recursos

| Recurso | Contenido |
| --- | --- |
| `view/welcome-view.fxml` | Pantalla inicial, logotipos y navegación principal. |
| `view/credits-view.fxml` | Información del curso, profesor, integrantes y botón para volver. |
| `view/dashboard-view.fxml` | Diseño del dashboard: barra superior, navegación lateral, chat y formulario de paciente. |
| `css/app.css` | Estilos compartidos de bienvenida, créditos y dashboard. |
| `images/medset-logo.png` | Logotipo del proyecto utilizado por las vistas. |
| `images/jala-logo.png` | Logotipo institucional utilizado en la bienvenida. |

Las vistas hacen referencia a controladores mediante `fx:controller` y enlazan
eventos con métodos FXML. `DashboardController` implementa las acciones
declaradas en `dashboard-view.fxml`. El chat llama al servicio en un
`CompletableFuture` y actualiza los controles JavaFX en el hilo de la interfaz.
El formulario del paciente todavía solo valida valores vacíos y escribe datos
en la consola; los informes y gráficos no están implementados.

## Dominio

| Clase | Estado actual |
| --- | --- |
| `model/Patient.java` | Marcador vacío para el futuro modelo de paciente simulado. |
| `model/Symptom.java` | Marcador vacío para el futuro modelo de síntoma. |
| `model/Pattern.java` | Marcador vacío para patrones de conjuntos. |
| `model/AnalysisResult.java` | Marcador vacío para el resultado de un análisis. |

Estas clases aún no definen campos, validaciones, operaciones de conjuntos ni
reglas clínicas o educativas.

## Servicios de inteligencia artificial

| Clase | Responsabilidad |
| --- | --- |
| `service/AiProvider.java` | Contrato común: generar una respuesta de texto y exponer el nombre del proveedor. |
| `service/AiService.java` | Fachada del proveedor seleccionado; permite enviar un prompt, consultar el nombre y reemplazar el proveedor. |
| `service/GeminiProvider.java` | Envía prompts al modelo Gemini con la biblioteca `google-genai`. Requiere `GEMINI_API_KEY`. |
| `service/OllamaProvider.java` | Envía una solicitud HTTP síncrona a `{OLLAMA_URL}/api/chat` y extrae el contenido de la respuesta. |

El dashboard crea un `AiService` con Ollama por defecto. El selector del chat
permite cambiar entre Ollama y Gemini. Las solicitudes se ejecutan en un hilo
de `CompletableFuture`, evitando bloquear el hilo de interfaz. El proveedor
Ollama hace una llamada HTTP síncrona dentro de ese hilo de trabajo.

La gestión de errores del chat muestra el error en la conversación. La
selección Gemini requiere configurar una clave API válida; su ausencia falla al
crear el proveedor.

## Configuración

`util/AppConfig.java` carga el archivo `.env` en la raíz mediante `dotenv-java`.
`GEMINI_API_KEY` no tiene valor predeterminado y produce una excepción si falta
cuando se solicita. Las otras propiedades tienen los valores predeterminados
descritos en el README. La biblioteca de dotenv está configurada para ignorar
que el archivo `.env` no exista; las claves opcionales entonces usan sus valores
predeterminados.

Usa `.env.example` como plantilla. `.env` y otros archivos `.env.*` están
ignorados por Git; `.env.example` es la excepción que sí se versiona.

## Dependencias y compilación

`pom.xml` define el artefacto Maven, Java 25, el plugin de JavaFX y las
dependencias JavaFX, ControlsFX, FormsFX, Ikonli, BootstrapFX, Google GenAI,
Jackson, dotenv-java y JUnit 5.

Comandos de uso habitual:

```text
bash ./mvnw test       Ejecuta las pruebas y compila el proyecto
bash ./mvnw javafx:run Ejecuta la aplicación JavaFX
```

En Windows se usa `.\mvnw.cmd` en vez de `./mvnw`.

## Flujo Git del equipo

- `main` es la rama de integración.
- Cada persona desarrolla en una rama propia, por ejemplo `dev-jose`.
- Los cambios llegan a `main` mediante pull request y revisión.
- Antes de abrir un pull request, ejecuta la verificación Maven y registra en
  su descripción lo que cambió, cómo se verificó y cualquier pendiente.
- El workflow `.github/workflows/maven.yml` verifica los pull requests y las
  actualizaciones de `main`.

## Pendientes conocidos

1. Completar los modelos vacíos y definir las operaciones educativas de teoría
   de conjuntos.
2. Añadir validación de entrada y conectar el formulario del paciente a un flujo
   educativo de análisis.
3. Definir y probar la generación del informe y la visualización gráfica antes
   de presentar esos controles como funcionales.
