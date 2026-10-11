package com.medset.medsetai.controller.logic;

import java.util.Set;
import java.util.TreeSet;

public class OperacionesConjuntos {

    public static Set<Integer> calcularUnion(Set<Integer> primerConjunto, Set<Integer> segundoConjunto) {
        Set<Integer> conjuntoFinal = new TreeSet<>();

            for (Integer numero : primerConjunto){
                conjuntoFinal.add(numero);
            }

            for (Integer numero : segundoConjunto){
                conjuntoFinal.add(numero);
            }
        return conjuntoFinal;
    }

    public static Set<Integer> calcularInterseccion(Set<Integer> primerConjunto, Set<Integer> segundoConjunto) {
        Set<Integer> conjuntoFinal = new TreeSet<>();

        for (Integer numero : primerConjunto){
            if (segundoConjunto.contains(numero)){
                conjuntoFinal.add(numero);
            }
        }
        return conjuntoFinal;
    }

    public static Set<Integer> calcularDiferencia(Set<Integer> primerConjunto, Set<Integer> segundoConjunto) {
        Set<Integer> conjuntoFinal = new TreeSet<>();

        for (Integer numero : primerConjunto){
            if (!segundoConjunto.contains(numero)){
                conjuntoFinal.add(numero);
            }
        }
        return conjuntoFinal;
    }

    public static Set<Integer> calcularDiferenciaSimetrica(Set<Integer> primerConjunto, Set<Integer> segundoConjunto) {
        Set<Integer> conjuntoFinal = new TreeSet<>();

        for (Integer numero : primerConjunto){
            if (!segundoConjunto.contains(numero)){
                conjuntoFinal.add(numero);
            }
        }

        for (Integer numero : segundoConjunto){
            if (!primerConjunto.contains(numero)){
                conjuntoFinal.add(numero);
            }
        }

        return conjuntoFinal;
    }

}