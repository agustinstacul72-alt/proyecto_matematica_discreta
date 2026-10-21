package com.medset.logica;

import java.util.EnumSet;
import java.util.Set;

/**
 * Calcula cada operación de la calculadora y arma los textos que se muestran.
 * No depende de la interfaz: la usan la app de escritorio y la versión web.
 */
public final class CalculadoraConjuntos {

    private CalculadoraConjuntos() {
    }

    /**
     * @param textoA       elementos de A separados por comas
     * @param textoSegundo elementos de B, o de U si la operación es el complemento
     */
    public static ResultadoOperacion calcular(Operacion operacion, String textoA, String textoSegundo) {
        Set<String> a = FormatoConjunto.leer(textoA);
        Set<String> segundo = FormatoConjunto.leer(textoSegundo);
        return switch (operacion) {
            case UNION -> union(a, segundo);
            case INTERSECCION -> interseccion(a, segundo);
            case DIFERENCIA -> diferencia(a, segundo);
            case SIMETRICA -> simetrica(a, segundo);
            case SUBCONJUNTO -> subconjunto(a, segundo);
            case COMPLEMENTO -> complemento(a, segundo);
        };
    }

    private static ResultadoOperacion union(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularUnion(a, b);
        int interseccion = OperacionesConjuntos.cardinalidad(OperacionesConjuntos.calcularInterseccion(a, b));
        return resultado(Operacion.UNION, r, cardinales(a, b)
                        + "\nPrincipio de inclusión-exclusión:\n|A ∪ B| = |A| + |B| − |A ∩ B| = "
                        + a.size() + " + " + b.size() + " − " + interseccion + " = " + r.size(),
                dos(a, b, EnumSet.of(ZonaVenn.SOLO_A, ZonaVenn.AMBOS, ZonaVenn.SOLO_B), false));
    }

    private static ResultadoOperacion interseccion(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularInterseccion(a, b);
        String disjuntos = r.isEmpty() ? "\nA ∩ B = ∅, así que A y B son disjuntos." : "";
        return resultado(Operacion.INTERSECCION, r, cardinales(a, b) + ",  |A ∩ B| = " + r.size() + disjuntos,
                dos(a, b, EnumSet.of(ZonaVenn.AMBOS), false));
    }

    private static ResultadoOperacion diferencia(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularDiferencia(a, b);
        Set<String> inversa = OperacionesConjuntos.calcularDiferencia(b, a);
        return resultado(Operacion.DIFERENCIA, r, cardinales(a, b) + ",  |A − B| = " + r.size()
                        + "\nOjo: la diferencia no es conmutativa. B − A = " + FormatoConjunto.formatear(inversa),
                dos(a, b, EnumSet.of(ZonaVenn.SOLO_A), false));
    }

    private static ResultadoOperacion simetrica(Set<String> a, Set<String> b) {
        Set<String> r = OperacionesConjuntos.calcularDiferenciaSimetrica(a, b);
        return resultado(Operacion.SIMETRICA, r, cardinales(a, b) + ",  |A Δ B| = " + r.size()
                        + "\nA − B = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, b))
                        + "   B − A = " + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(b, a)),
                dos(a, b, EnumSet.of(ZonaVenn.SOLO_A, ZonaVenn.SOLO_B), false));
    }

    private static ResultadoOperacion subconjunto(Set<String> a, Set<String> b) {
        if (OperacionesConjuntos.esSubconjunto(a, b)) {
            String propio = OperacionesConjuntos.sonIguales(a, b)
                    ? "Además A = B." : "Además A ⊂ B (subconjunto propio): B tiene elementos que A no tiene.";
            return new ResultadoOperacion("A ⊆ B es VERDADERO",
                    cardinales(a, b) + "\nTodo elemento de A pertenece a B. " + propio,
                    dos(a, b, EnumSet.of(ZonaVenn.AMBOS), false));
        }
        Set<String> contraejemplos = OperacionesConjuntos.calcularDiferencia(a, b);
        return new ResultadoOperacion("A ⊆ B es FALSO",
                cardinales(a, b) + "\nContraejemplos (están en A pero no en B): A − B = "
                        + FormatoConjunto.formatear(contraejemplos),
                dos(a, b, EnumSet.of(ZonaVenn.SOLO_A), true));
    }

    private static ResultadoOperacion complemento(Set<String> a, Set<String> u) {
        if (u.isEmpty()) {
            return new ResultadoOperacion("Definí primero el universo U.", "", null);
        }
        if (!OperacionesConjuntos.esSubconjunto(a, u)) {
            return new ResultadoOperacion("A debe ser subconjunto de U.", "Estos elementos de A no están en U: "
                    + FormatoConjunto.formatear(OperacionesConjuntos.calcularDiferencia(a, u)), null);
        }
        Set<String> r = OperacionesConjuntos.calcularComplemento(u, a);
        return new ResultadoOperacion("A' = " + FormatoConjunto.formatear(r),
                "|U| = " + u.size() + ",  |A| = " + a.size() + ",  |A'| = |U| − |A| = " + r.size()
                        + "\nSe cumple A ∪ A' = U  y  A ∩ A' = ∅.",
                new ResultadoOperacion.Diagrama(a, null, u, EnumSet.noneOf(ZonaVenn.class), false));
    }

    private static ResultadoOperacion resultado(Operacion operacion, Set<String> r, String detalle,
                                                ResultadoOperacion.Diagrama diagrama) {
        return new ResultadoOperacion(operacion.simbolo() + " = " + FormatoConjunto.formatear(r), detalle, diagrama);
    }

    private static ResultadoOperacion.Diagrama dos(Set<String> a, Set<String> b, Set<ZonaVenn> resaltar,
                                                   boolean alerta) {
        return new ResultadoOperacion.Diagrama(a, b, null, resaltar, alerta);
    }

    private static String cardinales(Set<String> a, Set<String> b) {
        return "|A| = " + OperacionesConjuntos.cardinalidad(a) + ",  |B| = " + OperacionesConjuntos.cardinalidad(b);
    }
}
