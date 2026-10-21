package com.medset.logica;

import java.util.LinkedHashSet;
import java.util.Set;


public final class OperacionesConjuntos {

    private OperacionesConjuntos() {
    }

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


    public static <T> Set<T> calcularInterseccion(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            if (segundoConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        return conjuntoFinal;
    }

    public static <T> Set<T> calcularDiferencia(Set<T> primerConjunto, Set<T> segundoConjunto) {
        Set<T> conjuntoFinal = new LinkedHashSet<>();
        for (T elemento : primerConjunto) {
            if (!segundoConjunto.contains(elemento)) {
                conjuntoFinal.add(elemento);
            }
        }
        return conjuntoFinal;
    }


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


    public static <T> Set<T> calcularComplemento(Set<T> universo, Set<T> conjunto) {
        return calcularDiferencia(universo, conjunto);
    }


    public static <T> boolean pertenece(T elemento, Set<T> conjunto) {
        return conjunto.contains(elemento);
    }


    public static <T> boolean esSubconjunto(Set<T> primerConjunto, Set<T> segundoConjunto) {
        for (T elemento : primerConjunto) {
            if (!segundoConjunto.contains(elemento)) {
                return false;
            }
        }
        return true;
    }

    /** A = B ⇔ A ⊆ B ∧ B ⊆ A */
    public static <T> boolean sonIguales(Set<T> primerConjunto, Set<T> segundoConjunto) {
        return esSubconjunto(primerConjunto, segundoConjunto)
                && esSubconjunto(segundoConjunto, primerConjunto);
    }


    public static <T> boolean sonDisjuntos(Set<T> primerConjunto, Set<T> segundoConjunto) {
        return calcularInterseccion(primerConjunto, segundoConjunto).isEmpty();
    }


    public static <T> int cardinalidad(Set<T> conjunto) {
        int contador = 0;
        for (T ignored : conjunto) {
            contador++;
        }
        return contador;
    }


    public static <T> double indiceJaccard(Set<T> primerConjunto, Set<T> segundoConjunto) {
        int union = cardinalidad(calcularUnion(primerConjunto, segundoConjunto));
        if (union == 0) {
            return 0.0;
        }
        int interseccion = cardinalidad(calcularInterseccion(primerConjunto, segundoConjunto));
        return (double) interseccion / union;
    }
}
