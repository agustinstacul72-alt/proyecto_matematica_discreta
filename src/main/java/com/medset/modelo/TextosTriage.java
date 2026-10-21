package com.medset.modelo;

import com.medset.logica.FormatoConjunto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Textos de la pantalla de triage. No dependen de la interfaz: la app de
 * escritorio y la versión web muestran exactamente lo mismo.
 */
public final class TextosTriage {

    public static final String SIN_DATOS = "SIN DATOS";
    public static final String SIN_DATOS_INDICACION =
            "Seleccioná al menos un síntoma. P = ∅ no se compara con ninguna enfermedad.";

    /** Pacientes que va mostrando el botón «Paciente de ejemplo», en orden. */
    public static final List<Set<String>> PACIENTES_DE_EJEMPLO = List.of(
            Set.of("fiebre", "tos", "dolor de cabeza", "cansancio"),
            Set.of("fiebre", "tos", "dolor de garganta", "cansancio"),
            Set.of("estornudos", "congestión nasal", "picazón en los ojos"),
            Set.of("fiebre", "tos", "dificultad para respirar", "dolor de pecho"),
            Set.of("fiebre", "dolor detrás de los ojos", "sarpullido", "dolor muscular"));

    /** Etiqueta de una tarjeta del ranking; {@code ok} se pinta en verde. */
    public record Etiqueta(String texto, boolean ok) {
    }

    private TextosTriage() {
    }

    public static String botonEdad(TipoPaciente t) {
        return t.nombre() + " · " + t.edades();
    }

    public static String riesgos(EvaluacionTriage e) {
        if (e.sintomasDeRiesgo().isEmpty()) {
            return "G = ∅: adulto sin condiciones especiales, así que la Regla 4 no aplica.";
        }
        return "G = síntomas de riesgo = " + FormatoConjunto.formatear(e.sintomasDeRiesgo())
                + "\nP ∩ G = " + FormatoConjunto.formatear(e.riesgos());
    }

    public static String paciente(Set<String> p) {
        return "P = " + FormatoConjunto.formatear(p) + "\n|P| = " + p.size()
                + " de |U| = " + CatalogoClinico.UNIVERSO.size();
    }

    public static String complemento(Set<String> p) {
        return "Síntomas descartados: P' = U − P tiene " + (CatalogoClinico.UNIVERSO.size() - p.size())
                + " elementos.";
    }

    public static String alarmas(EvaluacionTriage e) {
        return "R = síntomas de alarma = " + FormatoConjunto.formatear(CatalogoClinico.SINTOMAS_DE_ALARMA)
                + "\nP ∩ R = " + FormatoConjunto.formatear(e.alarmas());
    }

    public static String urgencia(NivelUrgencia nivel) {
        return "URGENCIA " + nivel.etiqueta().toUpperCase();
    }

    /** Clase de estilo del recuadro de urgencia: badge-baja, badge-media o badge-alta. */
    public static String estiloUrgencia(NivelUrgencia nivel) {
        return "badge-" + nivel.name().toLowerCase();
    }

    public static String tituloVenn(Enfermedad e) {
        return "Diagrama de Venn: P (paciente) frente a E = " + e.nombre();
    }

    public static String porcentaje(ResultadoAnalisis r) {
        return "J = " + r.porcentaje() + "%";
    }

    public static String detalle(ResultadoAnalisis r) {
        return "P ∩ E (coinciden) = " + FormatoConjunto.formatear(r.coincidencias())
                + "\nE − P (le faltan al paciente) = " + FormatoConjunto.formatear(r.faltantes())
                + "\nP − E (no los explica) = " + FormatoConjunto.formatear(r.noExplicados());
    }

    /** P y E no tienen síntomas en común: la tarjeta se muestra atenuada. */
    public static boolean atenuada(ResultadoAnalisis r) {
        return r.coincidencias().isEmpty();
    }

    public static List<Etiqueta> etiquetas(ResultadoAnalisis r) {
        List<Etiqueta> etiquetas = new ArrayList<>();
        if (r.coincidenciaExacta()) {
            etiquetas.add(new Etiqueta("P = E  coincidencia exacta", true));
        } else {
            if (r.pSubconjuntoE()) {
                etiquetas.add(new Etiqueta("P ⊆ E  explica todos los síntomas", true));
            }
            if (r.eSubconjuntoP()) {
                etiquetas.add(new Etiqueta("E ⊆ P  cuadro completo", true));
            }
            if (r.coincidencias().isEmpty()) {
                etiquetas.add(new Etiqueta("P ∩ E = ∅  disjuntos", false));
            }
        }
        etiquetas.add(new Etiqueta("urgencia base: " + r.enfermedad().urgencia().etiqueta(), false));
        return etiquetas;
    }
}
