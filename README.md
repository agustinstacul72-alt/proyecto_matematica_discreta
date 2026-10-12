# MedSet · Teoría de conjuntos aplicada al triage de síntomas

Proyecto final de **Matemáticas Discretas** · Jala University · Grupo 6

MedSet modela un problema real, la clasificación inicial (triage) de pacientes,
usando solo teoría de conjuntos:

- el **universo** U es el conjunto de todos los síntomas del catálogo;
- un **paciente** es un conjunto P ⊆ U;
- una **enfermedad** es un conjunto E ⊆ U;
- los **síntomas de alarma** forman un conjunto R ⊆ U.

Con unión, intersección, diferencia, complemento, inclusión y cardinalidad, el
sistema compara al paciente con cada enfermedad, las ordena y decide la urgencia.

> Es una herramienta **educativa**. Los datos clínicos están simplificados y no
> sirven para diagnosticar a nadie.

## Los tres módulos

| Módulo | Qué hace | Conceptos |
| --- | --- | --- |
| **1 · Calculadora** | Calcula operaciones entre dos conjuntos (números o palabras) y muestra la definición formal, la cardinalidad y un **diagrama de Venn** con el resultado resaltado. | ∪, ∩, −, Δ, ⊆, complemento A', \|A\|, inclusión-exclusión |
| **2 · Triage** | Marcás los síntomas del paciente y en vivo se calculan P ∩ E, E − P, P − E y el índice de Jaccard con cada enfermedad, se arma un ranking y se asigna la urgencia con tres reglas lógicas. | P ⊆ U, P ∩ R ≠ ∅, ⊆, J(P,E) = \|P ∩ E\| / \|P ∪ E\|, relaciones de orden |
| **3 · Juego detective** | El sistema esconde una enfermedad X. Preguntás «¿s ∈ X?» y el conjunto de candidatos C se filtra por comprensión. Cada pregunta cuesta puntos; la pista calcula la pregunta que mejor parte a C. | pertenencia, conjuntos por comprensión, particiones, búsqueda binaria |

| Calculadora | Triage | Juego |
| --- | --- | --- |
| ![Calculadora](docs/capturas/3_union.png) | ![Triage](docs/capturas/6_triage.png) | ![Juego](docs/capturas/8_juego.png) |

La explicación matemática completa, con ejemplos, está en
[`docs/CONCEPTOS.md`](docs/CONCEPTOS.md). La estructura del código está en
[`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

## Requisitos

- **JDK 21 o superior** (21 o 25, por ejemplo [Temurin](https://adoptium.net/)).
  Verificalo con `java -version`.
- No hace falta instalar Maven ni JavaFX: el *wrapper* `mvnw` los descarga la
  primera vez (se necesita internet).

## Cómo ejecutarlo

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
descargue las dependencias y ejecutá `Launcher.java`
(`src/main/java/com/medset/medsetai/Launcher.java`).

## Pruebas

```bash
bash ./mvnw test        # o .\mvnw.cmd test en Windows
```

Hay 18 pruebas JUnit, entre ellas las leyes de De Morgan, la no conmutatividad
de la diferencia, el principio de inclusión-exclusión, las reglas de urgencia y
que el juego siempre se puede resolver.

## Guion sugerido para la demo (5 minutos)

1. **Inicio (30 s):** la idea: paciente = conjunto P, enfermedad = conjunto E.
2. **Calculadora (1 min 30 s):** «Cargar ejemplo» en ∪ (mostrar inclusión-exclusión), en ⊆ con un contraejemplo en rojo y en A'.
3. **Triage (1 min 30 s):** «Paciente de ejemplo» varias veces; mostrar el ranking por Jaccard y cómo un síntoma de alarma sube la urgencia a ALTA (Regla 1: P ∩ R ≠ ∅).
4. **Juego (1 min 30 s):** jugar una ronda usando la pista y leer el registro «C ← { E ∈ C | s ∈ E }».

## Equipo

- Daniel Tueros Cueva
- Brisa Abigail Alvarez
- Stacul Dario Agustin
- Dayana Lucia Posada Narváez
- Agustin Tomas Gammuto
- Jose David Carranza Angarita

Profesor: Rafael Pastor Martínez Vargas
