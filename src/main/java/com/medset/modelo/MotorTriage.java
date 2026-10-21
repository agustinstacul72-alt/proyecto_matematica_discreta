package com.medset.modelo;

import com.medset.logica.FormatoConjunto;
import com.medset.logica.OperacionesConjuntos;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class MotorTriage {

    public static final double UMBRAL_COINCIDENCIA = 0.5;

    private static final Comparator<ResultadoAnalisis> POR_SIMILITUD = Comparator
            .comparingDouble(ResultadoAnalisis::similitud).reversed()
            .thenComparing(r -> r.enfermedad().nombre());

    private static final String REGLA_1 =
            "Regla 1: P ∩ R ≠ ∅ (síntomas de alarma presentes), así que la urgencia es ALTA.";
    private static final String REGLA_3 =
            "Regla 3: P ∩ R = ∅ y ninguna enfermedad alcanza J ≥ 50%; el cuadro es inespecífico y la urgencia es BAJA.";

    private final List<Enfermedad> enfermedades;
    private final Set<String> sintomasDeAlarma;

    public MotorTriage() {
        this(CatalogoClinico.ENFERMEDADES, CatalogoClinico.SINTOMAS_DE_ALARMA);
    }

    public MotorTriage(List<Enfermedad> enfermedades, Set<String> sintomasDeAlarma) {
        this.enfermedades = List.copyOf(enfermedades);
        this.sintomasDeAlarma = sintomasDeAlarma;
    }

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

    public List<ResultadoAnalisis> clasificar(Set<String> paciente) {
        return enfermedades.stream()
                .map(e -> comparar(paciente, e))
                .sorted(POR_SIMILITUD)
                .toList();
    }

    public Set<String> alarmasPresentes(Set<String> paciente) {
        return OperacionesConjuntos.calcularInterseccion(paciente, sintomasDeAlarma);
    }

    /** Triage de un adulto sin condiciones especiales. */
    public EvaluacionTriage evaluar(Set<String> paciente) {
        return evaluar(paciente, Set.of(TipoPaciente.ADULTO));
    }

    /** G = ⋃ G_t: síntomas de riesgo de todos los tipos del perfil. */
    public static Set<String> sintomasDeRiesgo(Set<TipoPaciente> perfil) {
        Set<String> g = Set.of();
        for (TipoPaciente t : perfil) {
            g = OperacionesConjuntos.calcularUnion(g, t.sintomasDeRiesgo());
        }
        return g;
    }

    /**
     * Aplica las reglas de urgencia calculando el ranking una sola vez.
     * Las reglas 1 a 3 dan el nivel base y la Regla 4 lo ajusta según el perfil.
     */
    public EvaluacionTriage evaluar(Set<String> paciente, Set<TipoPaciente> perfil) {
        Set<String> alarmas = alarmasPresentes(paciente);
        Set<String> g = sintomasDeRiesgo(perfil);
        Set<String> riesgos = OperacionesConjuntos.calcularInterseccion(paciente, g);
        List<ResultadoAnalisis> ranking = clasificar(paciente);

        NivelUrgencia base;
        String regla;
        ResultadoAnalisis mejor = ranking.isEmpty() ? null : ranking.get(0);
        if (!alarmas.isEmpty()) {
            base = NivelUrgencia.ALTA;
            regla = REGLA_1;
        } else if (mejor != null && mejor.similitud() >= UMBRAL_COINCIDENCIA) {
            base = mejor.enfermedad().urgencia();
            regla = "Regla 2: P ∩ R = ∅ y la mejor coincidencia (" + mejor.enfermedad().nombre()
                    + ", J = " + mejor.porcentaje() + "%) supera el umbral del 50%, así que se toma su urgencia.";
        } else {
            base = NivelUrgencia.BAJA;
            regla = REGLA_3;
        }

        NivelUrgencia nivel = base;
        if (!riesgos.isEmpty() && base != NivelUrgencia.ALTA) {
            nivel = base.subir();
            regla += "\nRegla 4: P ∩ G = " + FormatoConjunto.formatear(riesgos) + " ≠ ∅ ("
                    + motivos(perfil, riesgos) + "), así que la urgencia sube de "
                    + base.etiqueta().toUpperCase() + " a " + nivel.etiqueta().toUpperCase() + ".";
        }
        return new EvaluacionTriage(perfil, g, alarmas, riesgos, nivel, regla, ranking);
    }

    /** Motivos de los tipos del perfil que tienen algún síntoma de riesgo presente. */
    private static String motivos(Set<TipoPaciente> perfil, Set<String> riesgos) {
        return perfil.stream()
                .filter(t -> !OperacionesConjuntos.sonDisjuntos(t.sintomasDeRiesgo(), riesgos))
                .map(TipoPaciente::motivo)
                .collect(Collectors.joining("; "));
    }

    public NivelUrgencia nivelDeUrgencia(Set<String> paciente) {
        return evaluar(paciente).nivel();
    }

    public String explicarUrgencia(Set<String> paciente) {
        return evaluar(paciente).regla();
    }
}
