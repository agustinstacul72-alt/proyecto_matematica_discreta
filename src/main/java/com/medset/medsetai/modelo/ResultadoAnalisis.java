package com.medset.medsetai.modelo;

import java.util.Set;

/**
 * Comparación entre los síntomas del paciente P y una enfermedad E.
 *
 * @param enfermedad    la enfermedad E comparada
 * @param coincidencias P ∩ E: síntomas que el paciente tiene y la enfermedad explica
 * @param faltantes     E − P: síntomas típicos de E que el paciente no reporta
 * @param noExplicados  P − E: síntomas del paciente que E no explica
 * @param similitud     índice de Jaccard |P ∩ E| / |P ∪ E|
 * @param pSubconjuntoE P ⊆ E: la enfermedad explica todos los síntomas del paciente
 * @param eSubconjuntoP E ⊆ P: el paciente presenta el cuadro completo
 */
public record ResultadoAnalisis(
        Enfermedad enfermedad,
        Set<String> coincidencias,
        Set<String> faltantes,
        Set<String> noExplicados,
        double similitud,
        boolean pSubconjuntoE,
        boolean eSubconjuntoP) {

    /** P = E: coincidencia exacta. */
    public boolean coincidenciaExacta() {
        return pSubconjuntoE && eSubconjuntoP;
    }

    public int porcentaje() {
        return (int) Math.round(similitud * 100);
    }
}
