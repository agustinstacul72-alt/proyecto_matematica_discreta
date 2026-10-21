package com.medset.modelo;

import java.util.List;
import java.util.Set;

/**
 * Resultado completo del triage de un paciente P.
 *
 * @param perfil          tipos del paciente (grupo de edad y condiciones)
 * @param sintomasDeRiesgo G: unión de los síntomas de riesgo del perfil
 * @param alarmas         síntomas de alarma presentes, P ∩ R
 * @param riesgos         síntomas de riesgo presentes, P ∩ G
 * @param nivel           urgencia asignada
 * @param regla           explicación de las reglas que se aplicaron
 * @param ranking         enfermedades ordenadas de mayor a menor similitud
 */
public record EvaluacionTriage(
        Set<TipoPaciente> perfil,
        Set<String> sintomasDeRiesgo,
        Set<String> alarmas,
        Set<String> riesgos,
        NivelUrgencia nivel,
        String regla,
        List<ResultadoAnalisis> ranking) {

    public ResultadoAnalisis mejorCoincidencia() {
        return ranking.get(0);
    }
}
