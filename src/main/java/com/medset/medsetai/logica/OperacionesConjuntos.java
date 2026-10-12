package com.medset.medsetai.logica;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Operaciones de la teoría de conjuntos implementadas "a mano", recorriendo
 * los elementos con bucles en lugar de usar addAll/retainAll/removeAll.
 * Así cada método refleja directamente su definición formal.
 *
 * <p>Los métodos son genéricos: funcionan con números, palabras o síntomas.
 * Ninguno modifica los conjuntos recibidos; siempre devuelven uno nuevo.</p>
 */
public final class OperacionesConjuntos {

    private OperacionesConjuntos() {
    }

    /** A ∪ B = { x | x ∈ A ∨ x ∈ B } */
    public static <T> Set<T> calcularUnion(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            conjuntoFinal.add(elemento);
        }
        for (T elemento : segundoConjunto) {
            conjuntoFinal.add(elemento);
        }
        return conjuntoFinal;
    }

    /** A ∩ B = { x | x ∈ A ∧ x ∈ B } */
    public static <T> Set<T> calcularInterseccion(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            if (segundoConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        return conjuntoFinal;
    }

    /** A − B = { x | x ∈ A ∧ x ∉ B } */
    public static <T> Set<T> calcularDiferencia(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            if (!segundoConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        return conjuntoFinal;
    }

    /** A Δ B = (A − B) ∪ (B − A) */
    public static <T> Set<T> calcularDiferenciaSimetrica(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            if (!segundoConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        for (T elemento : segundoConjunto) {
            if (!primerConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        return conjuntoFinal;
    }

    /** A' = U − A = { x ∈ U | x ∉ A } */
    public static <T> Set<T> calcularComplemento(Set<T> universo, Set<T> conjunto) {
        return calcularDiferencia(universo, conjunto);
    }

    /** x ∈ A */
    public static <T> boolean pertenece(T elemento, Set<T> conjunto) {
        return conjunto.contains(elemento);
    }

    /** A ⊆ B ⇔ ∀x (x ∈ A → x ∈ B). El conjunto vacío es subconjunto de todo conjunto. */
    public static <T> boolean esSubconjunto(Set<T> primerConjunto, Set<T> segundoConjunto) {
        for (T elemento : primerConjunto) {
            if (!segundoConjunto.contains(elemento)) {
                return false; // encontramos un contraejemplo
            }
        }
        return true;
    }

    /** A = B ⇔ A ⊆ B ∧ B ⊆ A */
    public static <T> boolean sonIguales(Set<T> primerConjunto, Set<T> segundoConjunto) {
        return esSubconjunto(primerConjunto, segundoConjunto)
                && esSubconjunto(segundoConjunto, primerConjunto);
    }

    /** A y B son disjuntos ⇔ A ∩ B = ∅ */
    public static <T> boolean sonDisjuntos(Set<T> primerConjunto, Set<T> segundoConjunto) {
        return calcularInterseccion(primerConjunto, segundoConjunto).isEmpty();
    }

    /** |A|: cantidad de elementos distintos. */
    public static <T> int cardinalidad(Set<T> conjunto) {
        int contador = 0;
        for (T ignored : conjunto) {
            contador++;
        }
        return contador;
    }

    /**
     * Índice de Jaccard: J(A, B) = |A ∩ B| / |A ∪ B|, un número entre 0 y 1.
     * Vale 1 cuando A = B y 0 cuando son disjuntos. Por convención J(∅, ∅) = 0.
     */
    public static <T> double indiceJaccard(Set<T> primerConjunto, Set<T> segundoConjunto) {
        int union = cardinalidad(calcularUnion(primerConjunto, segundoConjunto));
        if (union == 0) {
            return 0.0;
        }
        int interseccion = cardinalidad(calcularInterseccion(primerConjunto, segundoConjunto));
        return (double) interseccion / union;
    }
}
