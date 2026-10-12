package com.medset.medsetai;

import com.medset.medsetai.logica.OperacionesConjuntos;
import com.medset.medsetai.modelo.CatalogoClinico;
import com.medset.medsetai.modelo.Enfermedad;
import com.medset.medsetai.modelo.MotorTriage;
import com.medset.medsetai.modelo.NivelUrgencia;
import com.medset.medsetai.modelo.ResultadoAnalisis;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorTriageTest {

    private final MotorTriage motor = new MotorTriage();

    @Test
    void catalogoConsistente() {
        Set<Set<String>> distintos = new HashSet<>();
        for (Enfermedad e : CatalogoClinico.ENFERMEDADES) {
            assertTrue(OperacionesConjuntos.esSubconjunto(e.sintomas(), CatalogoClinico.UNIVERSO),
                    e.nombre() + " debe cumplir E ⊆ U");
            assertTrue(distintos.add(e.sintomas()), "Dos enfermedades no pueden tener el mismo conjunto");
        }
        assertTrue(OperacionesConjuntos.esSubconjunto(CatalogoClinico.SINTOMAS_DE_ALARMA, CatalogoClinico.UNIVERSO));
    }

    @Test
    void coincidenciaExactaQuedaPrimera() {
        Enfermedad gripe = CatalogoClinico.ENFERMEDADES.get(2);
        List<ResultadoAnalisis> ranking = motor.clasificar(gripe.sintomas());
        assertEquals(gripe, ranking.get(0).enfermedad());
        assertTrue(ranking.get(0).coincidenciaExacta());
        assertEquals(100, ranking.get(0).porcentaje());
    }

    @Test
    void calculaInterseccionYDiferencias() {
        Set<String> p = Set.of("estornudos", "picazón en los ojos", "fiebre");
        ResultadoAnalisis r = motor.comparar(p, CatalogoClinico.ENFERMEDADES.get(1)); // alergia
        assertEquals(Set.of("estornudos", "picazón en los ojos"), r.coincidencias());
        assertEquals(Set.of("congestión nasal"), r.faltantes());
        assertEquals(Set.of("fiebre"), r.noExplicados());
        assertEquals(0.5, r.similitud(), 1e-9); // 2 / 4
    }

    @Test
    void sintomaDeAlarmaSubeUrgenciaAAlta() {
        Set<String> p = Set.of("estornudos", "congestión nasal", "dolor de pecho");
        assertEquals(NivelUrgencia.ALTA, motor.nivelDeUrgencia(p));
        assertEquals(Set.of("dolor de pecho"), motor.alarmasPresentes(p));
    }

    @Test
    void sinAlarmaUsaLaMejorCoincidencia() {
        Set<String> p = Set.of("náuseas", "vómito", "diarrea", "dolor abdominal");
        assertEquals(NivelUrgencia.MEDIA, motor.nivelDeUrgencia(p));
        Set<String> alergia = Set.of("estornudos", "congestión nasal", "picazón en los ojos");
        assertEquals(NivelUrgencia.BAJA, motor.nivelDeUrgencia(alergia));
    }

    @Test
    void cuadroInespecificoEsBaja() {
        Set<String> p = Set.of("sarpullido");
        assertEquals(NivelUrgencia.BAJA, motor.nivelDeUrgencia(p));
        assertNotEquals("", motor.explicarUrgencia(p));
        assertEquals(NivelUrgencia.BAJA, motor.nivelDeUrgencia(Set.of()));
    }
}
