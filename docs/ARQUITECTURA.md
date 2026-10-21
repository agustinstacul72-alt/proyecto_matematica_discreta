# Arquitectura y referencia del código

## Resumen

MedSet es una aplicación de escritorio en Java 21 con JavaFX y vistas FXML. La
lógica matemática está separada de la interfaz: los paquetes `logica` y
`modelo` no dependen de JavaFX y tienen pruebas unitarias propias.

```
src/main/java/com/medset/medsetai/
├── Launcher.java                 punto de entrada (main)
├── MedSetApplication.java        crea la ventana; con --smoke-test carga todas las vistas
├── logica/
│   ├── OperacionesConjuntos.java ∪ ∩ − Δ ', ⊆, ∈, =, disjuntos, |A|, Jaccard (genéricas)
│   └── FormatoConjunto.java      texto ⇄ conjunto, notación {a, b} / ∅
├── modelo/
│   ├── CatalogoClinico.java      U, R y D (síntomas, alarma, enfermedades)
│   ├── Enfermedad.java           record: nombre, E, urgencia base, descripción
│   ├── NivelUrgencia.java        BAJA < MEDIA < ALTA
│   ├── ResultadoAnalisis.java    P ∩ E, E − P, P − E, J, P ⊆ E, E ⊆ P
│   ├── MotorTriage.java          ranking por Jaccard y reglas de urgencia
│   └── JuegoDetective.java       estado y reglas del juego, pista óptima
├── ui/
│   └── VennDiagrama.java         diagrama de Venn dibujado con figuras JavaFX
├── controller/
│   ├── WelcomeController.java    navegación a los 3 módulos y a créditos
│   ├── DashboardController.java  Módulo 1: calculadora
│   ├── LaboratorioController.java Módulo 2: triage
│   ├── JuegoController.java      Módulo 3: juego
│   └── CreditsController.java    pantalla de integrantes
└── util/
    └── SceneManager.java         cambia de vista dentro de la misma ventana
```

## Vistas (`src/main/resources/com/medset/medsetai/view`)

| Vista | Controlador | Contenido |
| --- | --- | --- |
| `welcome-view.fxml` | `WelcomeController` | Logos, presentación del modelo y tarjetas de los 3 módulos |
| `dashboard-view.fxml` | `DashboardController` | Selector de 6 operaciones, formulario, resultado y diagrama de Venn |
| `laboratorio-view.fxml` | `LaboratorioController` | Botones de síntomas, urgencia, regla aplicada, Venn de P y E, ranking |
| `juego-view.fxml` | `JuegoController` | Candidatos, preguntas, pista, puntaje y registro del razonamiento |
| `credits-view.fxml` | `CreditsController` | Profesor e integrantes |

Los estilos están en `css/app.css`. Las partes dinámicas, como los botones de
síntomas, las tarjetas del ranking y el registro del juego, se crean desde el
controlador.

## Flujo de datos del Módulo 2

1. Cada `ToggleButton` de síntoma representa un elemento de U. Al cambiar uno se llama a `actualizar()`.
2. `paciente()` arma P con los botones seleccionados.
3. `MotorTriage.clasificar(P)` devuelve una lista de `ResultadoAnalisis` ordenada por Jaccard.
4. `MotorTriage.nivelDeUrgencia(P)` aplica las reglas 1, 2 y 3. `explicarUrgencia(P)` devuelve el texto que se muestra.
5. `VennDiagrama.dibujar(P, E, …)` dibuja P frente a la enfermedad seleccionada.

## Pruebas (`src/test/java`)

| Clase | Qué verifica |
| --- | --- |
| `OperacionesConjuntosTest` | Cada operación, De Morgan, ∅ ⊆ A, Jaccard y la lectura de texto |
| `MotorTriageTest` | Que el catálogo cumpla E ⊆ U y que no haya enfermedades repetidas, el ranking, las 3 reglas y el caso P = ∅ |
| `JuegoDetectiveTest` | El filtrado por pertenencia, el puntaje y que siguiendo la pista toda partida se resuelve en 4 preguntas o menos |

## Compilación

`pom.xml` usa Java 21 (`maven.compiler.release`) y JavaFX 21. Por eso compila con
JDK 21 o con cualquier versión posterior (25 incluida).

```text
bash ./mvnw test        Ejecuta las pruebas
bash ./mvnw javafx:run  Ejecuta la aplicación
```

En Windows se usa `.\mvnw.cmd` en lugar de `./mvnw`.

El workflow `.github/workflows/maven.yml` ejecuta las pruebas, empaqueta la
aplicación con `jpackage` y abre todas las vistas con `--smoke-test` en cada
pull request.

## Flujo Git del equipo

- `main` es la rama de integración.
- Cada persona trabaja en su propia rama y abre un pull request hacia `main`.
- Antes de abrir el PR, ejecutá `mvnw test`.
