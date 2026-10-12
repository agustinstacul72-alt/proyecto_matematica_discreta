package com.medset.medsetai;

import com.medset.medsetai.logica.FormatoConjunto;
import com.medset.medsetai.logica.OperacionesConjuntos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperacionesConjuntosTest {

    private final Set<String> a = Set.of("1", "2", "3", "4");
    private final Set<String> b = Set.of("3", "4", "5");
    private final Set<String> vacio = Set.of();

    @Test
    void union() {
        assertEquals(Set.of("1", "2", "3", "4", "5"), OperacionesConjuntos.calcularUnion(a, b));
        assertEquals(a, OperacionesConjuntos.calcularUnion(a, vacio)); // A ∪ ∅ = A
    }

    @Test
    void interseccion() {
        assertEquals(Set.of("3", "4"), OperacionesConjuntos.calcularInterseccion(a, b));
        assertTrue(OperacionesConjuntos.calcularInterseccion(a, vacio).isEmpty()); // A ∩ ∅ = ∅
    }

    @Test
    void diferenciaNoEsConmutativa() {
        assertEquals(Set.of("1", "2"), OperacionesConjuntos.calcularDiferencia(a, b));
        assertEquals(Set.of("5"), OperacionesConjuntos.calcularDiferencia(b, a));
    }

    @Test
    void diferenciaSimetricaEsUnionDeDiferencias() {
        Set<String> esperado = OperacionesConjuntos.calcularUnion(
                OperacionesConjuntos.calcularDiferencia(a, b),
                OperacionesConjuntos.calcularDiferencia(b, a));
        assertEquals(esperado, OperacionesConjuntos.calcularDiferenciaSimetrica(a, b));
        assertEquals(Set.of("1", "2", "5"), OperacionesConjuntos.calcularDiferenciaSimetrica(a, b));
    }

    @Test
    void complementoYLeyesDeDeMorgan() {
        Set<String> u = Set.of("1", "2", "3", "4", "5", "6");
        assertEquals(Set.of("5", "6"), OperacionesConjuntos.calcularComplemento(u, a));
        // (A ∪ B)' = A' ∩ B'
        assertEquals(
                OperacionesConjuntos.calcularComplemento(u, OperacionesConjuntos.calcularUnion(a, b)),
                OperacionesConjuntos.calcularInterseccion(
                        OperacionesConjuntos.calcularComplemento(u, a),
                        OperacionesConjuntos.calcularComplemento(u, b)));
        // (A ∩ B)' = A' ∪ B'
        assertEquals(
                OperacionesConjuntos.calcularComplemento(u, OperacionesConjuntos.calcularInterseccion(a, b)),
                OperacionesConjuntos.calcularUnion(
                        OperacionesConjuntos.calcularComplemento(u, a),
                        OperacionesConjuntos.calcularComplemento(u, b)));
    }

    @Test
    void subconjuntoPertenenciaYDisjuntos() {
        assertTrue(OperacionesConjuntos.esSubconjunto(Set.of("3", "4"), a));
        assertFalse(OperacionesConjuntos.esSubconjunto(b, a));
        assertTrue(OperacionesConjuntos.esSubconjunto(vacio, a)); // ∅ ⊆ A
        assertTrue(OperacionesConjuntos.pertenece("2", a));
        assertFalse(OperacionesConjuntos.pertenece("9", a));
        assertTrue(OperacionesConjuntos.sonDisjuntos(Set.of("1"), Set.of("2")));
        assertTrue(OperacionesConjuntos.sonIguales(Set.of("1", "2"), Set.of("2", "1")));
    }

    @Test
    void cardinalidadYJaccard() {
        assertEquals(4, OperacionesConjuntos.cardinalidad(a));
        assertEquals(2.0 / 5.0, OperacionesConjuntos.indiceJaccard(a, b), 1e-9);
        assertEquals(1.0, OperacionesConjuntos.indiceJaccard(a, a), 1e-9);
        assertEquals(0.0, OperacionesConjuntos.indiceJaccard(vacio, vacio), 1e-9);
    }

    @Test
    void lecturaYFormato() {
        assertEquals(Set.of("1", "2", "3"), FormatoConjunto.leer("{1, 2, 2, 3,}"));
        assertTrue(FormatoConjunto.leer("  ").isEmpty());
        assertEquals("{2, 10, fiebre}", FormatoConjunto.formatear(Set.of("fiebre", "10", "2")));
        assertEquals("∅", FormatoConjunto.formatear(vacio));
    }
}
