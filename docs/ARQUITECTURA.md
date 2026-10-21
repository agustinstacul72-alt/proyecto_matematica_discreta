# Arquitectura y referencia del código

## Resumen

MedSet es una aplicación de escritorio en Java 25 con JavaFX 25 y vistas FXML. La
lógica matemática está separada de la interfaz: los paquetes `logica` y
`modelo` no dependen de JavaFX y tienen pruebas unitarias propias.

```
src/main/java/module-info.java    módulo com.medset (abre los controladores a FXML)
src/main/java/com/medset/
├── Launcher.java                 punto de entrada (main)
├── MedSetApplication.java        crea la ventana; con --smoke-test carga todas las vistas
├── logica/
│   ├── OperacionesConjuntos.java ∪ ∩ − Δ ', ⊆, ∈, =, disjuntos, |A|, Jaccard (genéricas)
│   ├── FormatoConjunto.java      texto ⇄ conjunto, notación {a, b} / ∅
│   ├── Operacion.java            las 6 operaciones: símbolo, definición y ejemplo
│   ├── CalculadoraConjuntos.java calcula cada operación y arma sus textos
│   ├── ResultadoOperacion.java   resultado, detalle y qué dibujar en el diagrama
│   └── ZonaVenn.java             SOLO_A, AMBOS, SOLO_B
├── modelo/
│   ├── CatalogoClinico.java      U, R y D (síntomas, alarma, enfermedades)
│   ├── Enfermedad.java           record: nombre, E, urgencia base, descripción
│   ├── NivelUrgencia.java        BAJA < MEDIA < ALTA, subir() para la Regla 4
│   ├── TipoPaciente.java         grupos de edad y condiciones, cada uno con su G ⊆ U
│   ├── ResultadoAnalisis.java    P ∩ E, E − P, P − E, J, P ⊆ E, E ⊆ P
│   ├── EvaluacionTriage.java     resultado del triage: P ∩ R, P ∩ G, nivel, regla y ranking
│   ├── MotorTriage.java          ranking por Jaccard y reglas de urgencia 1 a 4
│   ├── TextosTriage.java         textos de la pantalla de triage y pacientes de ejemplo
│   ├── JuegoDetective.java       estado y reglas del juego, pista óptima
│   └── TextosJuego.java          mensajes y registro del juego
├── ui/
│   └── VennDiagrama.java         diagrama de Venn dibujado con figuras JavaFX
├── controller/
│   ├── WelcomeController.java    navegación a los 3 módulos y a créditos
│   ├── DashboardController.java  Módulo 1: calculadora
│   ├── LaboratorioController.java Módulo 2: triage
│   ├── JuegoController.java      Módulo 3: juego
│   └── CreditsController.java    pantalla de integrantes
└── util/
    ├── Vista.java                las 5 pantallas y su archivo FXML
    └── SceneManager.java         carga vistas y las cambia dentro de la misma escena
```

## Vistas (`src/main/resources/com/medset/view`)

| Vista | Controlador | Contenido |
| --- | --- | --- |
| `welcome-view.fxml` | `WelcomeController` | Logos, presentación del modelo y tarjetas de los 3 módulos |
| `dashboard-view.fxml` | `DashboardController` | Selector de 6 operaciones, formulario, resultado y diagrama de Venn |
| `laboratorio-view.fxml` | `LaboratorioController` | Tipo de paciente, botones de síntomas, urgencia, reglas aplicadas, Venn de P y E, ranking |
| `juego-view.fxml` | `JuegoController` | Candidatos, preguntas, pista, puntaje y registro del razonamiento |
| `credits-view.fxml` | `CreditsController` | Profesor e integrantes |

Los estilos están en `css/app.css`. Las partes dinámicas, como los botones de
síntomas, las tarjetas del ranking y el registro del juego, se crean desde el
controlador.

## Flujo de datos del Módulo 2

1. Cada `ToggleButton` de síntoma representa un elemento de U. Al cambiar uno se llama a `actualizar()`.
   Los botones de «Paciente de ejemplo» y «Limpiar» cambian varios síntomas y recalculan una sola vez.
2. `paciente()` arma P con los botones seleccionados y `perfil()` arma el tipo de paciente
   (un grupo de edad más las condiciones marcadas).
3. `MotorTriage.evaluar(P, perfil)` calcula una sola vez el ranking por Jaccard, aplica las reglas
   1 a 4 y devuelve un `EvaluacionTriage` con todo lo que muestra la pantalla.
4. Los textos de la pantalla salen de `TextosTriage`.
5. `VennDiagrama.dibujar(P, E, …)` dibuja P frente a la enfermedad seleccionada.

## Pruebas (`src/test/java`)

| Clase | Qué verifica |
| --- | --- |
| `OperacionesConjuntosTest` | Cada operación, De Morgan, ∅ ⊆ A, Jaccard y la lectura de texto |
| `MotorTriageTest` | Que el catálogo cumpla E ⊆ U y que no haya enfermedades repetidas, el ranking, las 4 reglas, la unión de los síntomas de riesgo y el caso P = ∅ |
| `JuegoDetectiveTest` | El filtrado por pertenencia, el puntaje y que siguiendo la pista toda partida se resuelve en 4 preguntas o menos |
| `CalculadoraConjuntosTest` | Los textos de la calculadora (inclusión-exclusión, contraejemplos, validación del universo) y que cada ejemplo dibuje su diagrama |

## Compilación

`pom.xml` usa Java 25 (`maven.compiler.release`) y JavaFX 25, así que hace falta
JDK 25 o posterior. JavaFX solo necesita `javafx-controls` y `javafx-fxml`.

Java 25 avisa cuando una librería carga código nativo sin permiso. Para que la
aplicación arranque sin advertencias, `--enable-native-access` se pasa en todos
los modos de ejecución:

| Modo | Dónde está configurado |
| --- | --- |
| `mvnw javafx:run` | `<options>` del `javafx-maven-plugin` en `pom.xml` |
| IntelliJ IDEA | configuración de ejecución compartida `.run/MedSet.run.xml` |
| Instaladores | `--java-options` de `jpackage` en los workflows |
| Maven (su propia consola) | `.mvn/jvm.config` |

Los íconos de los instaladores están en `src/main/packaging/`: `medset.ico`
(Windows), `medset.icns` (macOS) y `medset.png` (Linux). La ventana usa
`images/medset-icon.png`.

```text
bash ./mvnw test        Ejecuta las pruebas
bash ./mvnw javafx:run  Ejecuta la aplicación
```

En Windows se usa `.\mvnw.cmd` en lugar de `./mvnw`.

El workflow `.github/workflows/maven.yml` ejecuta las pruebas, empaqueta la
aplicación con `jpackage` y abre todas las vistas con `--smoke-test` en cada
pull request.

## Versión web (`web/`)

La web no reescribe la lógica: compila **las mismas clases** de `logica/` y
`modelo/` a JavaScript con [TeaVM](https://teavm.org/). Lo que muestra cada
pantalla (resultados, detalles, reglas, mensajes del juego) se arma en Java en
`CalculadoraConjuntos`, `TextosTriage` y `TextosJuego`, que usan tanto los
controladores JavaFX como la web. Así las dos versiones muestran lo mismo.

```
web/
├── pom.xml                         copia logica/ y modelo/ y los compila con TeaVM
├── src/main/java/com/medset/web/
│   ├── MedSetWeb.java              funciones exportadas a JavaScript; devuelven JSON
│   └── Json.java                   arma el JSON sin librerías
└── src/main/webapp/
    ├── index.html
    ├── css/app.css                 los mismos colores y tamaños que css/app.css de JavaFX
    └── js/
        ├── app.js                  las 5 pantallas y la navegación (#calculadora, #triage, …)
        └── venn.js                 diagrama de Venn en SVG, con la geometría de VennDiagrama.java
```

`bash ./mvnw -f web/pom.xml package` deja el sitio en `web/target/sitio`. El
workflow `.github/workflows/pages.yml` corre las pruebas, arma el sitio y lo
publica en GitHub Pages cada vez que se actualiza `main`.

## Flujo Git del equipo

- `main` es la rama de integración.
- Cada persona trabaja en su propia rama y abre un pull request hacia `main`.
- Antes de abrir el PR, ejecutá `mvnw test`.
