package com.medset.medsetai.logica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Convierte texto escrito por el usuario en conjuntos y conjuntos en texto
 * con notación matemática ({a, b, c} o ∅).
 */
public final class FormatoConjunto {

    private FormatoConjunto() {
    }

    /** Orden de presentación: primero los números (de menor a mayor), luego el texto alfabético. */
    public static final Comparator<Object> ORDEN_NATURAL = (x, y) -> {
        String a = String.valueOf(x);
        String b = String.valueOf(y);
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

    /**
     * Lee un conjunto escrito como "1, 2, 3" o "{fiebre, tos}". Los elementos
     * repetidos se ignoran porque un conjunto no tiene elementos duplicados.
     */
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

    /** Devuelve "{a, b, c}" ordenado, o "∅" si el conjunto está vacío. */
    public static String formatear(Set<?> conjunto) {
        if (conjunto.isEmpty()) {
            return "∅";
        }
        List<Object> ordenados = new ArrayList<>(conjunto);
        ordenados.sort(ORDEN_NATURAL);
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < ordenados.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ordenados.get(i));
        }
        return sb.append('}').toString();
    }

    /** Devuelve los elementos ordenados para mostrarlos en pantalla. */
    public static List<String> ordenar(Set<?> conjunto) {
        List<Object> ordenados = new ArrayList<>(conjunto);
        ordenados.sort(ORDEN_NATURAL);
        List<String> textos = new ArrayList<>();
        for (Object o : ordenados) {
            textos.add(String.valueOf(o));
        }
        return textos;
    }

    private static Double comoNumero(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
