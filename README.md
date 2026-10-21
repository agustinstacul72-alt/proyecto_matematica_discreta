<div align="center">

<img src="src/main/resources/com/medset/medsetai/images/medset-logo.png" alt="Logo de MedSet" width="180">

# MedSet

### Teoría de conjuntos aplicada al triage de síntomas

Proyecto final de **Matemáticas Discretas** · Grupo 6

![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-1565C0)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white)
![Pruebas](https://img.shields.io/badge/JUnit-18%20pruebas-25A162?logo=junit5&logoColor=white)

<img src="src/main/resources/com/medset/medsetai/images/jala-logo.png" alt="Jala University" width="200">

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

Marcás los síntomas del paciente y en vivo se calculan P ∩ E, E − P, P − E y el
índice de Jaccard con cada enfermedad. Con eso se arma un ranking y se asigna la
urgencia con tres reglas lógicas.

**Conceptos:** P ⊆ U, P ∩ R ≠ ∅, ⊆, J(P,E) = |P ∩ E| / |P ∪ E|, relaciones de orden.

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

## Cómo ejecutarlo

**Requisitos:** JDK 21 o superior (por ejemplo [Temurin](https://adoptium.net/)).
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
descargue las dependencias y ejecutá
[`Launcher.java`](src/main/java/com/medset/medsetai/Launcher.java).

## Pruebas

```bash
bash ./mvnw test        # o .\mvnw.cmd test en Windows
```

Hay 18 pruebas JUnit: leyes de De Morgan, no conmutatividad de la diferencia,
principio de inclusión-exclusión, reglas de urgencia y que el juego siempre se
puede resolver.

## Estructura del proyecto

```
src/main/java/com/medset/medsetai/
├── logica/       Operaciones de conjuntos (∪ ∩ − Δ ' ⊆ ∈ |A| Jaccard)
├── modelo/       Catálogo clínico, MotorTriage, JuegoDetective
├── controller/   Controladores de cada pantalla (JavaFX)
└── ui/           Diagrama de Venn
```

Documentación completa:

- [`docs/CONCEPTOS.md`](docs/CONCEPTOS.md): la matemática del proyecto, con ejemplos.
- [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md): cómo está organizado el código.

<div align="center">
  <sub>Jala University · Matemáticas Discretas</sub>
</div>
