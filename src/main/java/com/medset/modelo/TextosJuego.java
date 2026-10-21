package com.medset.modelo;

import com.medset.logica.FormatoConjunto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mensajes y registro del juego «Detective de síntomas». No dependen de la
 * interfaz: la app de escritorio y la versión web muestran exactamente lo mismo.
 */
public final class TextosJuego {

    public static final String MENSAJE_INICIAL = "Elegí un síntoma para preguntar.";
    public static final String SIN_PUNTOS = "Te quedaste sin puntos.";
    public static final String RENDICION = "Te rendiste.";

    private TextosJuego() {
    }

    public static String registroInicio(JuegoDetective juego) {
        return "Inicio: C = D (las " + juego.candidatos().size() + " enfermedades del catálogo).";
    }

    /** Texto del botón de un síntoma ya preguntado. */
    public static String botonPreguntado(String sintoma, boolean si) {
        return sintoma + (si ? "  ✓" : "  ✗");
    }

    public static String registroPregunta(String sintoma, boolean si, int antes, List<Enfermedad> candidatos) {
        return "¿" + sintoma + " ∈ X?  " + (si ? "Sí" : "No") + ".  C ← { E ∈ C | " + sintoma + " " + (si ? "∈" : "∉")
                + " E }  →  |C| pasó de " + antes + " a " + candidatos.size() + "  = " + nombres(candidatos);
    }

    public static String respuestaPregunta(String sintoma, boolean si, List<Enfermedad> candidatos) {
        String respuesta = si ? "Sí, la enfermedad secreta tiene «" + sintoma + "»."
                : "No, la enfermedad secreta no tiene «" + sintoma + "».";
        if (candidatos.size() == 1) {
            respuesta += " Solo queda un candidato: ¡adiviná!";
        }
        return respuesta;
    }

    public static String registroAcierto(Enfermedad e) {
        return "Adivinaste X = " + e.nombre() + ".  X = " + FormatoConjunto.formatear(e.sintomas());
    }

    public static String mensajeAcierto(Enfermedad e, int puntos) {
        return "¡Correcto! Era " + e.nombre() + ". Ganaste " + puntos + " puntos.";
    }

    public static String registroError(Enfermedad e) {
        return "Intentaste " + e.nombre() + ": incorrecto (−" + JuegoDetective.COSTO_ERROR + "). Se quita de C.";
    }

    public static String mensajeError(Enfermedad e) {
        return "No es " + e.nombre() + ". Seguí investigando.";
    }

    public static String pista(JuegoDetective juego) {
        String mejor = juego.mejorPregunta();
        if (mejor == null) {
            return "Pista: ya no hace falta preguntar; solo queda " + nombres(juego.candidatos()) + ".";
        }
        int con = juego.candidatosCon(mejor);
        int sin = juego.candidatos().size() - con;
        return "Pista: preguntá por «" + mejor + "». Divide a C en " + con + " candidatos que lo tienen y "
                + sin + " que no, así que en el peor caso te quedan " + Math.max(con, sin) + ".";
    }

    public static String mensajeDerrota(String motivo, Enfermedad secreta) {
        return motivo + " La enfermedad secreta era " + secreta.nombre() + ".";
    }

    public static String registroRespuesta(Enfermedad secreta) {
        return "Respuesta: X = " + secreta.nombre() + " = " + FormatoConjunto.formatear(secreta.sintomas());
    }

    public static String puntos(JuegoDetective juego) {
        return "Puntos de la ronda: " + juego.puntos();
    }

    public static String total(int puntajeTotal, int rondas) {
        return "Total: " + puntajeTotal + " pts en " + rondas + " rondas";
    }

    public static String tituloCandidatos(JuegoDetective juego) {
        return "Candidatos C   (|C| = " + juego.candidatos().size() + ")";
    }

    /** Una línea del registro con su número: "3. ¿fiebre ∈ X? …". */
    public static String lineaRegistro(int numero, String linea) {
        return numero + ". " + linea;
    }

    public static String nombres(List<Enfermedad> lista) {
        if (lista.isEmpty()) {
            return "∅";
        }
        return lista.stream().map(Enfermedad::nombre).collect(Collectors.joining(", ", "{", "}"));
    }
}
