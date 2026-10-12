package com.medset.medsetai.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Catálogo educativo y simplificado de síntomas y enfermedades.
 * NO es información médica para diagnosticar: sirve para practicar teoría de conjuntos.
 */
public final class CatalogoClinico {

    private CatalogoClinico() {
    }

    /** U: universo de síntomas. Todo conjunto de síntomas del sistema es subconjunto de U. */
    public static final Set<String> UNIVERSO = conjunto(
            "fiebre", "tos", "dolor de cabeza", "dolor de garganta", "cansancio",
            "congestión nasal", "estornudos", "dolor muscular", "escalofríos",
            "pérdida del olfato", "dificultad para respirar", "dolor de pecho",
            "náuseas", "vómito", "diarrea", "dolor abdominal", "sarpullido",
            "picazón en los ojos", "dolor detrás de los ojos", "confusión");

    /**
     * R: síntomas de alarma. Si P ∩ R ≠ ∅ el paciente pasa a urgencia ALTA,
     * sin importar qué enfermedad coincida mejor.
     */
    public static final Set<String> SINTOMAS_DE_ALARMA = conjunto(
            "dificultad para respirar", "dolor de pecho", "confusión");

    /** D: conjunto de enfermedades del catálogo. Cada E ∈ D cumple E ⊆ U. */
    public static final List<Enfermedad> ENFERMEDADES = List.of(
            new Enfermedad("Resfriado común",
                    conjunto("congestión nasal", "estornudos", "dolor de garganta", "tos", "cansancio"),
                    NivelUrgencia.BAJA,
                    "Infección viral leve de las vías respiratorias altas."),
            new Enfermedad("Alergia estacional",
                    conjunto("estornudos", "congestión nasal", "picazón en los ojos"),
                    NivelUrgencia.BAJA,
                    "Reacción del sistema inmune a polen, polvo u otros alérgenos."),
            new Enfermedad("Gripe (influenza)",
                    conjunto("fiebre", "escalofríos", "dolor muscular", "dolor de cabeza", "cansancio", "tos"),
                    NivelUrgencia.MEDIA,
                    "Infección viral con inicio brusco y malestar general."),
            new Enfermedad("COVID-19",
                    conjunto("fiebre", "tos", "cansancio", "pérdida del olfato", "dificultad para respirar", "dolor de cabeza"),
                    NivelUrgencia.MEDIA,
                    "Infección por SARS-CoV-2; la pérdida del olfato es característica."),
            new Enfermedad("Gastroenteritis",
                    conjunto("náuseas", "vómito", "diarrea", "dolor abdominal", "fiebre"),
                    NivelUrgencia.MEDIA,
                    "Inflamación del estómago e intestino; vigilar la hidratación."),
            new Enfermedad("Dengue",
                    conjunto("fiebre", "dolor de cabeza", "dolor detrás de los ojos", "dolor muscular", "sarpullido", "náuseas"),
                    NivelUrgencia.MEDIA,
                    "Enfermedad viral transmitida por el mosquito Aedes aegypti."),
            new Enfermedad("Neumonía",
                    conjunto("fiebre", "tos", "dificultad para respirar", "dolor de pecho", "escalofríos", "cansancio"),
                    NivelUrgencia.ALTA,
                    "Infección de los pulmones; requiere valoración médica pronta."));

    private static Set<String> conjunto(String... elementos) {
        Set<String> s = new LinkedHashSet<>();
        Collections.addAll(s, elementos);
        return Collections.unmodifiableSet(s);
    }
}
