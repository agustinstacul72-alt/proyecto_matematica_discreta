# Conceptos de Matemáticas Discretas en MedSet

Este documento explica cada concepto que usa la aplicación, dónde aparece en
pantalla y en qué parte del código está implementado.

## 1. Modelo: todo es un conjunto

| Símbolo | Significado | En el código |
| --- | --- | --- |
| U | Universo: los 20 síntomas del catálogo | `CatalogoClinico.UNIVERSO` |
| P | Síntomas del paciente, P ⊆ U | se arma con los botones del Módulo 2 |
| E | Síntomas característicos de una enfermedad, E ⊆ U | `Enfermedad.sintomas()` |
| D | Conjunto de enfermedades del catálogo (7) | `CatalogoClinico.ENFERMEDADES` |
| R | Síntomas de alarma = {dificultad para respirar, dolor de pecho, confusión} | `CatalogoClinico.SINTOMAS_DE_ALARMA` |

Un conjunto no tiene orden ni elementos repetidos. Por eso, si en la calculadora
escribís `1, 2, 2, 3`, el conjunto que se guarda es {1, 2, 3}
(`FormatoConjunto.leer`).

## 2. Operaciones (Módulo 1 · `OperacionesConjuntos`)

Todas están implementadas recorriendo los elementos con bucles, sin usar
`addAll`/`retainAll`, para que el código siga la definición formal paso a paso.

| Operación | Definición | Ejemplo con A = {1,2,3,4}, B = {3,4,5} |
| --- | --- | --- |
| Unión | A ∪ B = { x \| x ∈ A ∨ x ∈ B } | {1,2,3,4,5} |
| Intersección | A ∩ B = { x \| x ∈ A ∧ x ∈ B } | {3,4} |
| Diferencia | A − B = { x \| x ∈ A ∧ x ∉ B } | {1,2} (y B − A = {5}: **no es conmutativa**) |
| Diferencia simétrica | A Δ B = (A − B) ∪ (B − A) | {1,2,5} |
| Complemento | A' = U − A = { x ∈ U \| x ∉ A } | con U = {1,…,6}: A' = {5,6} |
| Inclusión | A ⊆ B ⇔ ∀x (x ∈ A → x ∈ B) | {3,4} ⊆ A es verdadero |
| Igualdad | A = B ⇔ A ⊆ B ∧ B ⊆ A | |
| Disjuntos | A ∩ B = ∅ | |
| Cardinalidad | \|A\| = número de elementos | \|A\| = 4 |

**Conexión con la lógica proposicional:** la unión corresponde al «o» (∨), la
intersección al «y» (∧) y el complemento al «no» (¬). La inclusión es una
implicación con cuantificador universal. Por eso `esSubconjunto` se detiene en
cuanto encuentra un **contraejemplo**: un x ∈ A con x ∉ B. En pantalla, los
contraejemplos aparecen en rojo en el diagrama de Venn.

**Principio de inclusión-exclusión:** al calcular A ∪ B, la aplicación muestra
\|A ∪ B\| = \|A\| + \|B\| − \|A ∩ B\|. Se resta la intersección porque esos
elementos se contaron dos veces.

**Leyes verificadas con pruebas** (`OperacionesConjuntosTest`):
De Morgan, (A ∪ B)' = A' ∩ B' y (A ∩ B)' = A' ∪ B';
identidades A ∪ ∅ = A y A ∩ ∅ = ∅; ∅ ⊆ A para cualquier A.

**Diagrama de Venn** (`ui/VennDiagrama`): divide el universo en cuatro regiones,
A − B, A ∩ B, B − A y U − (A ∪ B), escribe los elementos de cada una y pinta la
región del resultado. Las regiones se calculan con las mismas operaciones de
conjuntos.

## 3. Triage (Módulo 2 · `MotorTriage`)

Para cada enfermedad E ∈ D se calcula:

| Expresión | Interpretación clínica |
| --- | --- |
| P ∩ E | síntomas del paciente que la enfermedad explica |
| E − P | síntomas típicos de E que el paciente **no** tiene |
| P − E | síntomas del paciente que E **no** explica |
| P ⊆ E | E explica *todos* los síntomas del paciente |
| E ⊆ P | el paciente tiene el cuadro *completo* de E |
| P = E | coincidencia exacta |

### Índice de Jaccard

J(P, E) = \|P ∩ E\| / \|P ∪ E\|, un número entre 0 y 1. Vale 1 si P = E y 0 si
P y E son disjuntos. Contar solo \|P ∩ E\| no alcanza, porque favorecería a las
enfermedades con muchos síntomas. Jaccard penaliza tanto lo que sobra como lo
que falta.

Las enfermedades se **ordenan** de mayor a menor J (con empate, alfabéticamente).
Es una relación de orden total sobre D.

### Reglas de urgencia (lógica proposicional)

1. **Regla 1:** si P ∩ R ≠ ∅ → urgencia **ALTA** (hay un síntoma de alarma).
2. **Regla 2:** si P ∩ R = ∅ y la mejor coincidencia tiene J ≥ 0,5 → se usa la urgencia de esa enfermedad.
3. **Regla 3:** en cualquier otro caso → **BAJA** (cuadro inespecífico).

Las reglas se evalúan en orden, y la primera que se cumple decide. La pantalla
muestra cuál regla se aplicó.

### Ejemplo resuelto

P = {fiebre, tos, dolor de cabeza, cansancio}

- Gripe: E = {fiebre, escalofríos, dolor muscular, dolor de cabeza, cansancio, tos}.
  P ∩ E = P (4 elementos), \|P ∪ E\| = 6, así que J = 4/6 ≈ 67 %. Además P ⊆ E.
- COVID-19 también da J = 4/6 ≈ 67 % (empate).
- Resfriado: P ∩ E = {tos, cansancio}, \|P ∪ E\| = 4 + 5 − 2 = 7, así que J = 2/7 ≈ 29 %.
- P ∩ R = ∅, así que no aplica la Regla 1. La mejor J es 0,67 ≥ 0,5, así que se aplica la Regla 2 y la urgencia es **MEDIA**.

Si se agrega «dolor de pecho», entonces P ∩ R = {dolor de pecho} ≠ ∅ y la urgencia
pasa a **ALTA**, aunque la mejor coincidencia siga siendo de urgencia media.

## 4. Juego «Detective de síntomas» (Módulo 3 · `JuegoDetective`)

- El sistema elige X ∈ D al azar.
- Al principio, el conjunto de candidatos es C = D.
- Cada pregunta es una **prueba de pertenencia** «¿s ∈ X?». La respuesta filtra
  C usando **notación por comprensión**:
  - si s ∈ X: C ← { E ∈ C \| s ∈ E }
  - si s ∉ X: C ← { E ∈ C \| s ∉ E }
- Cada pregunta separa a C en dos subconjuntos disjuntos cuya unión es C, es
  decir, una **partición**. La pista elige el síntoma que minimiza el peor caso,
  max(\|con s\|, \|sin s\|). Es la misma idea que la búsqueda binaria: con 7
  enfermedades bastan ⌈log₂ 7⌉ = 3 preguntas en el mejor reparto, y el juego
  garantiza como máximo 4 (está probado en `JuegoDetectiveTest`).
- Puntaje: se empieza con 100. Cada pregunta resta 10 y cada intento fallido resta 25.

Al terminar, el registro de la pantalla muestra todo el razonamiento en notación
de conjuntos.

## 5. Limitaciones

- El catálogo es pequeño y está simplificado. No tiene en cuenta la intensidad
  ni la duración de los síntomas, ni su peso relativo.
- Jaccard trata a todos los síntomas con el mismo peso. Una mejora natural sería
  usar conjuntos difusos o asignar pesos a los síntomas.
