package com.medset.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Características del paciente que cambian su urgencia. Hay grupos de edad
 * (se elige uno) y condiciones (se pueden sumar al grupo de edad).
 *
 * <p>Cada tipo tiene su conjunto G ⊆ U de síntomas de riesgo: síntomas que en
 * ese tipo de paciente se valoran antes que en un adulto sano (Regla 4).</p>
 */
public enum TipoPaciente {
    NINO("Niño", "0 a 12 años", false,
            "en niños la fiebre, el vómito y la diarrea deshidratan rápido",
            "fiebre", "vómito", "diarrea", "sarpullido"),
    ADULTO("Adulto", "13 a 59 años", false,
            "sin agravantes por edad"),
    ADULTO_MAYOR("Adulto mayor", "60 años o más", false,
            "en adultos mayores las infecciones y la deshidratación se complican antes",
            "fiebre", "vómito", "diarrea", "dolor abdominal"),
    EMBARAZO("Embarazo", "cualquier edad", true,
            "en el embarazo la fiebre, el dolor abdominal y el dolor de cabeza requieren control",
            "fiebre", "dolor abdominal", "dolor de cabeza", "vómito"),
    DISCAPACIDAD("Discapacidad", "cualquier edad", true,
            "a una persona con discapacidad puede costarle expresar el malestar y descompensarse antes",
            "fiebre", "vómito", "diarrea", "cansancio");

    private final String nombre;
    private final String edades;
    private final boolean condicion;
    private final String motivo;
    private final Set<String> sintomasDeRiesgo;

    TipoPaciente(String nombre, String edades, boolean condicion, String motivo, String... sintomasDeRiesgo) {
        this.nombre = nombre;
        this.edades = edades;
        this.condicion = condicion;
        this.motivo = motivo;
        this.sintomasDeRiesgo = Collections.unmodifiableSet(new LinkedHashSet<>(List.of(sintomasDeRiesgo)));
    }

    public String nombre() {
        return nombre;
    }

    public String edades() {
        return edades;
    }

    /** {@code true} para condiciones (embarazo, discapacidad); {@code false} para grupos de edad. */
    public boolean esCondicion() {
        return condicion;
    }

    /** Por qué este tipo de paciente tiene esos síntomas de riesgo. */
    public String motivo() {
        return motivo;
    }

    /** G: síntomas de riesgo. Es ∅ para los adultos. */
    public Set<String> sintomasDeRiesgo() {
        return sintomasDeRiesgo;
    }

    public static List<TipoPaciente> gruposDeEdad() {
        return List.of(values()).stream().filter(t -> !t.condicion).toList();
    }

    public static List<TipoPaciente> condiciones() {
        return List.of(values()).stream().filter(t -> t.condicion).toList();
    }
}
