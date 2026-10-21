package com.medset.logica;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class FormatoConjunto {

    private FormatoConjunto() {
    }

    /** Números primero (en orden numérico) y después palabras (en orden alfabético). */
    private static final Comparator<String> ORDEN_NATURAL = (a, b) -> {
        Double na = comoNumero(a);
        Double nb = comoNumero(b);
        if (na != null && nb != null) {
            return Double.compare(na, nb);
        }
        if (na != null) {
            return -1;
        }
        if (nb != null) {
            return 1;
        }
        return a.compareToIgnoreCase(b);
    };

    /** Convierte un texto como "{a, b, c}" o "a, b, c" en un conjunto. */
    public static Set<String> leer(String texto) {
        Set<String> conjunto = new LinkedHashSet<>();
        if (texto == null) {
            return conjunto;
        }
        String limpio = texto.replace("{", "").replace("}", "").trim();
        if (limpio.isEmpty() || limpio.equals("∅")) {
            return conjunto;
        }
        for (String parte : limpio.split(",")) {
            String elemento = parte.trim().toLowerCase();
            if (!elemento.isEmpty()) {
                conjunto.add(elemento);
            }
        }
        return conjunto;
    }

    /** Escribe el conjunto en notación matemática: "{a, b, c}" o "∅". */
    public static String formatear(Set<?> conjunto) {
        if (conjunto.isEmpty()) {
            return "∅";
        }
        return "{" + String.join(", ", ordenar(conjunto)) + "}";
    }

    public static List<String> ordenar(Set<?> conjunto) {
        return conjunto.stream()
                .map(String::valueOf)
                .sorted(ORDEN_NATURAL)
                .toList();
    }

    private static Double comoNumero(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
