package com.medset.medsetai.modelo;

import com.medset.medsetai.logica.OperacionesConjuntos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Motor de triage basado exclusivamente en operaciones de conjuntos.
 *
 * <ol>
 *   <li>Para cada enfermedad E ∈ D calcula P ∩ E, E − P, P − E, J(P, E), P ⊆ E y E ⊆ P.</li>
 *   <li>Ordena las enfermedades por similitud de Jaccard (relación de orden).</li>
 *   <li>Asigna la urgencia con reglas lógicas:
 *     <ul>
 *       <li>Regla 1: si P ∩ R ≠ ∅ (hay síntomas de alarma) → ALTA.</li>
 *       <li>Regla 2: si no, se toma la urgencia de la mejor coincidencia cuando J ≥ 0.5.</li>
 *       <li>Regla 3: en otro caso → BAJA (cuadro inespecífico).</li>
 *     </ul>
 *   </li>
 * </ol>
 */
public final class MotorTriage {

    /** Umbral mínimo de similitud para considerar que una enfermedad "coincide". */
    public static final double UMBRAL_COINCIDENCIA = 0.5;

    private final List<Enfermedad> enfermedades;
    private final Set<String> sintomasDeAlarma;

    public MotorTriage() {
        this(CatalogoClinico.ENFERMEDADES, CatalogoClinico.SINTOMAS_DE_ALARMA);
    }

    public MotorTriage(List<Enfermedad> enfermedades, Set<String> sintomasDeAlarma) {
        this.enfermedades = List.copyOf(enfermedades);
        this.sintomasDeAlarma = sintomasDeAlarma;
    }

    /** Compara P con una sola enfermedad E. */
    public ResultadoAnalisis comparar(Set<String> paciente, Enfermedad enfermedad) {
        Set<String> e = enfermedad.sintomas();
        return new ResultadoAnalisis(
                enfermedad,
                OperacionesConjuntos.calcularInterseccion(paciente, e),
                OperacionesConjuntos.calcularDiferencia(e, paciente),
                OperacionesConjuntos.calcularDiferencia(paciente, e),
                OperacionesConjuntos.indiceJaccard(paciente, e),
                OperacionesConjuntos.esSubconjunto(paciente, e),
                OperacionesConjuntos.esSubconjunto(e, paciente));
    }

    /** Devuelve todas las enfermedades ordenadas de mayor a menor similitud con P. */
    public List<ResultadoAnalisis> clasificar(Set<String> paciente) {
        List<ResultadoAnalisis> resultados = new ArrayList<>();
        for (Enfermedad e : enfermedades) {
            resultados.add(comparar(paciente, e));
        }
        resultados.sort(Comparator
                .comparingDouble(ResultadoAnalisis::similitud).reversed()
                .thenComparing(r -> r.enfermedad().nombre()));
        return resultados;
    }

    /** P ∩ R: síntomas de alarma presentes en el paciente. */
    public Set<String> alarmasPresentes(Set<String> paciente) {
        return OperacionesConjuntos.calcularInterseccion(paciente, sintomasDeAlarma);
    }

    /** Aplica las reglas de prioridad descritas en la documentación de la clase. */
    public NivelUrgencia nivelDeUrgencia(Set<String> paciente) {
        if (!OperacionesConjuntos.sonDisjuntos(paciente, sintomasDeAlarma)) {
            return NivelUrgencia.ALTA;                                      // Regla 1
        }
        List<ResultadoAnalisis> ranking = clasificar(paciente);
        if (!ranking.isEmpty() && ranking.get(0).similitud() >= UMBRAL_COINCIDENCIA) {
            return ranking.get(0).enfermedad().urgencia();                  // Regla 2
        }
        return NivelUrgencia.BAJA;                                          // Regla 3
    }

    /** Explica en texto qué regla se aplicó. */
    public String explicarUrgencia(Set<String> paciente) {
        Set<String> alarmas = alarmasPresentes(paciente);
        if (!alarmas.isEmpty()) {
            return "Regla 1: P ∩ R ≠ ∅ (síntomas de alarma presentes), así que la urgencia es ALTA.";
        }
        List<ResultadoAnalisis> ranking = clasificar(paciente);
        ResultadoAnalisis mejor = ranking.get(0);
        if (mejor.similitud() >= UMBRAL_COINCIDENCIA) {
            return "Regla 2: P ∩ R = ∅ y la mejor coincidencia (" + mejor.enfermedad().nombre()
                    + ", J = " + mejor.porcentaje() + "%) supera el umbral del 50%, así que se toma su urgencia.";
        }
        return "Regla 3: P ∩ R = ∅ y ninguna enfermedad alcanza J ≥ 50%; el cuadro es inespecífico y la urgencia es BAJA.";
    }
}
