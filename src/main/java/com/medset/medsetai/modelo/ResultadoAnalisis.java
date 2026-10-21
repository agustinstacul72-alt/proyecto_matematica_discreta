package com.medset.medsetai.modelo;

import java.util.Set;


public record ResultadoAnalisis(
        Enfermedad enfermedad,
        Set<String> coincidencias,
        Set<String> faltantes,
        Set<String> noExplicados,
        double similitud,
        boolean pSubconjuntoE,
        boolean eSubconjuntoP) {


    public boolean coincidenciaExacta() {
        return pSubconjuntoE && eSubconjuntoP;
    }

    public int porcentaje() {
        return (int) Math.round(similitud * 100);
    }
}
