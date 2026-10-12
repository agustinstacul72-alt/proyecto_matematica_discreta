package com.medset.medsetai.modelo;

import com.medset.medsetai.logica.OperacionesConjuntos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Juego "Detective de síntomas".
 *
 * <p>El sistema elige en secreto una enfermedad X del catálogo D. El jugador
 * pregunta por síntomas: cada pregunta es una prueba de pertenencia s ∈ X.
 * Con cada respuesta el conjunto de candidatos C se filtra por comprensión:</p>
 * <pre>
 *   si s ∈ X:  C ← { E ∈ C | s ∈ E }
 *   si s ∉ X:  C ← { E ∈ C | s ∉ E }
 * </pre>
 * <p>Cada pregunta cuesta puntos, así que conviene elegir síntomas que partan C
 * en dos mitades lo más parecidas posible (como una búsqueda binaria).</p>
 */
public final class JuegoDetective {

    public static final int PUNTOS_INICIALES = 100;
    public static final int COSTO_PREGUNTA = 10;
    public static final int COSTO_ERROR = 25;

    private final List<Enfermedad> catalogo;
    private final Random random;

    private Enfermedad secreta;
    private List<Enfermedad> candidatos;
    private final Set<String> preguntados = new LinkedHashSet<>();
    private int puntos;
    private boolean terminado;
    private boolean ganado;

    public JuegoDetective() {
        this(CatalogoClinico.ENFERMEDADES, new Random());
    }

    public JuegoDetective(List<Enfermedad> catalogo, Random random) {
        this.catalogo = List.copyOf(catalogo);
        this.random = random;
        nuevaPartida();
    }

    /** Inicia una ronda con una enfermedad secreta al azar. */
    public void nuevaPartida() {
        iniciarCon(catalogo.get(random.nextInt(catalogo.size())));
    }

    /** Inicia una ronda con una enfermedad secreta concreta (útil para pruebas). */
    public void iniciarCon(Enfermedad enfermedad) {
        secreta = enfermedad;
        candidatos = new ArrayList<>(catalogo);
        preguntados.clear();
        puntos = PUNTOS_INICIALES;
        terminado = false;
        ganado = false;
    }

    /**
     * Pregunta si la enfermedad secreta tiene el síntoma: s ∈ X.
     * Descuenta puntos y filtra los candidatos.
     *
     * @return true si el síntoma pertenece a la enfermedad secreta
     */
    public boolean preguntar(String sintoma) {
        if (terminado) {
            throw new IllegalStateException("La partida ya terminó.");
        }
        if (!preguntados.add(sintoma)) {
            throw new IllegalArgumentException("Ese síntoma ya fue preguntado.");
        }
        boolean pertenece = OperacionesConjuntos.pertenece(sintoma, secreta.sintomas());
        List<Enfermedad> filtrados = new ArrayList<>();
        for (Enfermedad e : candidatos) {
            if (OperacionesConjuntos.pertenece(sintoma, e.sintomas()) == pertenece) {
                filtrados.add(e);
            }
        }
        candidatos = filtrados;
        puntos = Math.max(0, puntos - COSTO_PREGUNTA);
        return pertenece;
    }

    /**
     * Intenta adivinar la enfermedad secreta.
     *
     * @return true si acertó; en ese caso la partida termina
     */
    public boolean adivinar(Enfermedad propuesta) {
        if (terminado) {
            throw new IllegalStateException("La partida ya terminó.");
        }
        if (propuesta.equals(secreta)) {
            terminado = true;
            ganado = true;
            return true;
        }
        candidatos.remove(propuesta);
        puntos = Math.max(0, puntos - COSTO_ERROR);
        if (puntos == 0) {
            terminado = true;
        }
        return false;
    }

    /** Se rinde y revela la respuesta. */
    public void rendirse() {
        terminado = true;
        ganado = false;
        puntos = 0;
    }

    /**
     * Pista: el síntoma no preguntado que mejor divide a los candidatos, es decir,
     * el que minimiza max(|C ∩ con s|, |C ∩ sin s|). Devuelve null si no hay
     * ningún síntoma que distinga a los candidatos.
     */
    public String mejorPregunta() {
        String mejor = null;
        int mejorPeorCaso = Integer.MAX_VALUE;
        for (String s : CatalogoClinico.UNIVERSO) {
            if (preguntados.contains(s)) {
                continue;
            }
            int con = 0;
            for (Enfermedad e : candidatos) {
                if (e.sintomas().contains(s)) {
                    con++;
                }
            }
            int sin = candidatos.size() - con;
            if (con == 0 || sin == 0) {
                continue; // no distingue a nadie
            }
            int peorCaso = Math.max(con, sin);
            if (peorCaso < mejorPeorCaso) {
                mejorPeorCaso = peorCaso;
                mejor = s;
            }
        }
        return mejor;
    }

    public List<Enfermedad> candidatos() {
        return List.copyOf(candidatos);
    }

    public Set<String> preguntados() {
        return Set.copyOf(preguntados);
    }

    public int puntos() {
        return puntos;
    }

    public boolean terminado() {
        return terminado;
    }

    public boolean ganado() {
        return ganado;
    }

    public Enfermedad secreta() {
        return secreta;
    }

    public List<Enfermedad> catalogo() {
        return catalogo;
    }
}
