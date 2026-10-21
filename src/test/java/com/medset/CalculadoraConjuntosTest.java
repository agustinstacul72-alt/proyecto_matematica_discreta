package com.medset;

import com.medset.logica.CalculadoraConjuntos;
import com.medset.logica.Operacion;
import com.medset.logica.ResultadoOperacion;
import com.medset.logica.ZonaVenn;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraConjuntosTest {

    @Test
    void unionMuestraInclusionExclusion() {
        ResultadoOperacion r = CalculadoraConjuntos.calcular(Operacion.UNION, "1, 2, 3", "3, 4");
        assertEquals("A ∪ B = {1, 2, 3, 4}", r.resultado());
        assertTrue(r.detalle().contains("|A ∪ B| = |A| + |B| − |A ∩ B| = 3 + 2 − 1 = 4"));
        assertEquals(Set.of(ZonaVenn.SOLO_A, ZonaVenn.AMBOS, ZonaVenn.SOLO_B), r.diagrama().resaltar());
    }

    @Test
    void subconjuntoFalsoMarcaContraejemplosEnRojo() {
        ResultadoOperacion r = CalculadoraConjuntos.calcular(Operacion.SUBCONJUNTO, "1, 5", "1, 2, 3");
        assertEquals("A ⊆ B es FALSO", r.resultado());
        assertTrue(r.detalle().endsWith("A − B = {5}"));
        assertTrue(r.diagrama().alerta());
    }

    @Test
    void complementoValidaElUniverso() {
        assertNull(CalculadoraConjuntos.calcular(Operacion.COMPLEMENTO, "1", "").diagrama());
        assertEquals("A debe ser subconjunto de U.",
                CalculadoraConjuntos.calcular(Operacion.COMPLEMENTO, "9", "1, 2").resultado());

        ResultadoOperacion r = CalculadoraConjuntos.calcular(Operacion.COMPLEMENTO, "1, 2", "1, 2, 3, 4");
        assertEquals("A' = {3, 4}", r.resultado());
        assertTrue(r.diagrama().esComplemento());
    }

    @Test
    void cadaOperacionTieneUnEjemploQueSeCalcula() {
        for (Operacion op : Operacion.values()) {
            ResultadoOperacion r = CalculadoraConjuntos.calcular(op, op.ejemploA(), op.ejemploSegundo());
            assertFalse(r.resultado().isEmpty(), op.name());
            assertTrue(r.diagrama() != null, op.name() + " debe dibujar el diagrama con su ejemplo");
        }
    }
}
