<div align="center">

<img src="src/main/resources/com/medset/images/medset-logo.png" alt="Logo de MedSet" width="180">

# MedSet

### Teoría de conjuntos aplicada al triage de síntomas

Proyecto final de **Matemáticas Discretas** · Grupo 6

![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-25-1565C0)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white)
![Pruebas](https://img.shields.io/badge/JUnit-26%20pruebas-25A162?logo=junit5&logoColor=white)

<img src="src/main/resources/com/medset/images/jala-logo.png" alt="Jala University" width="200">

</div>

---

## Equipo · Grupo 6

- Daniel Tueros Cueva
- Brisa Abigail Alvarez
- Stacul Dario Agustin
- Dayana Lucia Posada Narváez
- Agustin Tomas Gammuto
- Jose David Carranza Angarita

**Profesor:** Rafael Pastor Martínez Vargas

## ¿Qué es MedSet?

MedSet modela un problema real, la clasificación inicial (**triage**) de
pacientes, usando solo teoría de conjuntos:

| Símbolo | Significado |
| :---: | --- |
| **U** | Universo: todos los síntomas del catálogo |
| **P ⊆ U** | Un paciente es el conjunto de sus síntomas |
| **E ⊆ U** | Una enfermedad es el conjunto de sus síntomas típicos |
| **R ⊆ U** | Síntomas de alarma |

Con unión, intersección, diferencia, complemento, inclusión y cardinalidad, el
sistema compara al paciente con cada enfermedad, las ordena por parecido y
decide la urgencia.

> [!NOTE]
> Es una herramienta **educativa**. Los datos clínicos están simplificados y no
> sirven para diagnosticar a nadie.

<div align="center">
  <img src="docs/capturas/1_inicio.png" alt="Pantalla de inicio" width="700">
</div>

## Los tres módulos

### 1 · Calculadora de conjuntos

Calcula operaciones entre dos conjuntos (números o palabras) y muestra la
definición formal, la cardinalidad y un **diagrama de Venn** con el resultado
resaltado.

**Conceptos:** ∪, ∩, −, Δ, ⊆, complemento A', |A|, inclusión-exclusión.

| Unión | Subconjunto |
| :---: | :---: |
| ![Unión](docs/capturas/3_union.png) | ![Subconjunto](docs/capturas/4_subconjunto.png) |

### 2 · Triage

Elegís el **tipo de paciente** (niño, adulto o adulto mayor, y si tiene embarazo
o discapacidad) y marcás sus síntomas. En vivo se calculan P ∩ E, E − P, P − E y
el índice de Jaccard con cada enfermedad. Con eso se arma un ranking y se asigna
la urgencia con cuatro reglas lógicas. La cuarta sube la urgencia si el paciente
tiene síntomas de riesgo para su grupo: P ∩ G ≠ ∅, con G = G_edad ∪ G_condiciones.

**Conceptos:** P ⊆ U, P ∩ R ≠ ∅, P ∩ G ≠ ∅, unión de conjuntos, ⊆, J(P,E) = |P ∩ E| / |P ∪ E|, relaciones de orden.

| Triage | Urgencia alta |
| :---: | :---: |
| ![Triage](docs/capturas/6_triage.png) | ![Urgencia alta](docs/capturas/7_triage_alta.png) |

### 3 · Juego «Detective de síntomas»

El sistema esconde una enfermedad X. Preguntás «¿s ∈ X?» y el conjunto de
candidatos C se filtra por comprensión. Cada pregunta cuesta puntos; la pista
calcula la pregunta que mejor parte a C.

**Conceptos:** pertenencia, conjuntos por comprensión, particiones, búsqueda binaria.

<div align="center">
  <img src="docs/capturas/8_juego.png" alt="Juego detective" width="700">
</div>

## Usar en el navegador

MedSet también funciona en la web, sin instalar nada y desde el celular:

**https://agustinstacul72-alt.github.io/proyecto_matematica_discreta/**

La versión web usa **el mismo código Java** de la app de escritorio (`logica/`
y `modelo/`), compilado a JavaScript con [TeaVM](https://teavm.org/). Por eso
calcula exactamente lo mismo: las operaciones, el índice de Jaccard, las cuatro
reglas de urgencia y el juego. Solo la interfaz está hecha en HTML y CSS.

## Descargar

En [Releases](https://github.com/agustinstacul72-alt/proyecto_matematica_discreta/releases/latest)
hay instaladores listos que no necesitan Java instalado:

| Sistema | Archivo |
| --- | --- |
| Windows | `MedSet-x.y.z.exe` o `MedSet-x.y.z.msi` |
| macOS | `MedSet-x.y.z.pkg` |
| Linux (Debian / Ubuntu) | `medset_x.y.z_amd64.deb` |

> [!TIP]
> En macOS, si el sistema avisa que la app es de un desarrollador no
> identificado, abrila con clic derecho → **Abrir**.

## Cómo ejecutarlo desde el código

**Requisitos:** JDK 25 o superior (por ejemplo [Temurin](https://adoptium.net/)).
Verificalo con `java -version`. No hace falta instalar Maven ni JavaFX: el
*wrapper* `mvnw` los descarga la primera vez (se necesita internet).

Desde la carpeta del proyecto:

```powershell
# Windows (PowerShell)
.\mvnw.cmd javafx:run
```

```bash
# macOS / Linux
bash ./mvnw javafx:run
```

**Desde IntelliJ IDEA:** abrí la carpeta como proyecto Maven, esperá a que
descargue las dependencias y elegí la configuración de ejecución **MedSet**
(arriba a la derecha, junto al botón ▶). Ya trae la opción
`--enable-native-access=javafx.graphics`, que Java 25 necesita para que JavaFX
arranque sin advertencias.

**Versión web en tu computador:**

```bash
bash ./mvnw -f web/pom.xml package                           # o .\mvnw.cmd -f web\pom.xml package
python -m http.server 8765 --directory web/target/sitio      # abrí http://localhost:8765
```

## Pruebas

```bash
bash ./mvnw test        # o .\mvnw.cmd test en Windows
```

Hay 26 pruebas JUnit: leyes de De Morgan, no conmutatividad de la diferencia,
principio de inclusión-exclusión, los textos de la calculadora, las cuatro reglas
de urgencia (incluido el tipo de paciente) y que el juego siempre se puede resolver.

## Estructura del proyecto

```
src/main/java/com/medset/
├── logica/       Operaciones de conjuntos y calculadora     ← compartido con la web
├── modelo/       Catálogo, tipos de paciente, triage, juego ← compartido con la web
├── controller/   Controladores de cada pantalla (JavaFX)
├── ui/           Diagrama de Venn (JavaFX)
└── util/         Navegación entre pantallas
web/
├── src/main/java/com/medset/web/   Puente Java → JavaScript (MedSetWeb)
└── src/main/webapp/                Páginas, estilos y diagrama de Venn web
```

Documentación completa:

- [`docs/CONCEPTOS.md`](docs/CONCEPTOS.md): la matemática del proyecto, con ejemplos.
- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md): cómo está organizado el código.

<div align="center">
  <sub>Jala University · Matemáticas Discretas</sub>
</div>
