package com.medset.modelo;

import com.medset.logica.OperacionesConjuntos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    public void rendirse() {
        terminado = true;
        ganado = false;
        puntos = 0;
    }

    /**
     * Pregunta que mejor parte a C: la que deja menos candidatos en el peor caso.
     * Devuelve {@code null} si ningún síntoma distingue a los candidatos.
     */
    public String mejorPregunta() {
        String mejor = null;
        int mejorPeorCaso = Integer.MAX_VALUE;
        for (String s : CatalogoClinico.UNIVERSO) {
            if (preguntados.contains(s)) {
                continue;
            }
            int con = candidatosCon(s);
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

    /** Cantidad de candidatos que tienen el síntoma: |{ E ∈ C | s ∈ E }|. */
    public int candidatosCon(String sintoma) {
        int con = 0;
        for (Enfermedad e : candidatos) {
            if (OperacionesConjuntos.pertenece(sintoma, e.sintomas())) {
                con++;
            }
        }
        return con;
    }

    public List<Enfermedad> candidatos() {
        return List.copyOf(candidatos);
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
