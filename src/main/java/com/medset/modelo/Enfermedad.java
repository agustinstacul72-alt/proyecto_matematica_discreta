package com.medset.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public record Enfermedad(String nombre, Set<String> sintomas, NivelUrgencia urgencia, String descripcion) {

    public Enfermedad {
        sintomas = Collections.unmodifiableSet(new LinkedHashSet<>(sintomas));
    }

    @Override
    public String toString() {
        return nombre;
    }
}
