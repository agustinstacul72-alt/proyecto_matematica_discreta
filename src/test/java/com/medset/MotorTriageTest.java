package com.medset;

import com.medset.logica.OperacionesConjuntos;
import com.medset.modelo.CatalogoClinico;
import com.medset.modelo.Enfermedad;
import com.medset.modelo.MotorTriage;
import com.medset.modelo.NivelUrgencia;
import com.medset.modelo.ResultadoAnalisis;
import com.medset.modelo.TipoPaciente;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void ninoConFiebreSubeUnNivel() {
        Set<String> p = Set.of("fiebre", "tos", "dolor de cabeza", "cansancio"); // gripe, MEDIA
        assertEquals(NivelUrgencia.MEDIA, motor.evaluar(p, Set.of(TipoPaciente.ADULTO)).nivel());
        var nino = motor.evaluar(p, Set.of(TipoPaciente.NINO));
        assertEquals(NivelUrgencia.ALTA, nino.nivel());
        assertEquals(Set.of("fiebre"), nino.riesgos());
        assertTrue(nino.regla().contains("Regla 4"));
    }

    @Test
    void condicionesSeSumanAlGrupoDeEdad() {
        Set<String> p = Set.of("estornudos", "congestión nasal", "picazón en los ojos", "fiebre"); // alergia, BAJA
        assertEquals(NivelUrgencia.BAJA, motor.evaluar(p).nivel());
        assertEquals(NivelUrgencia.MEDIA, motor.evaluar(p, Set.of(TipoPaciente.ADULTO_MAYOR)).nivel());
        assertEquals(NivelUrgencia.MEDIA,
                motor.evaluar(p, Set.of(TipoPaciente.ADULTO, TipoPaciente.DISCAPACIDAD)).nivel());
        // G = G_adulto_mayor ∪ G_discapacidad
        assertEquals(Set.of("fiebre", "vómito", "diarrea", "dolor abdominal", "cansancio"),
                MotorTriage.sintomasDeRiesgo(Set.of(TipoPaciente.ADULTO_MAYOR, TipoPaciente.DISCAPACIDAD)));
    }

    @Test
    void regla4NoAplicaSiPYGSonDisjuntosOYaEsAlta() {
        Set<String> alergia = Set.of("estornudos", "congestión nasal", "picazón en los ojos");
        assertEquals(NivelUrgencia.BAJA, motor.evaluar(alergia, Set.of(TipoPaciente.ADULTO, TipoPaciente.EMBARAZO)).nivel());

        Set<String> conAlarma = Set.of("fiebre", "dolor de pecho");
        var nino = motor.evaluar(conAlarma, Set.of(TipoPaciente.NINO));
        assertEquals(NivelUrgencia.ALTA, nino.nivel());
        assertFalse(nino.regla().contains("Regla 4"));
    }

    @Test
    void todosLosSintomasDeRiesgoEstanEnElUniverso() {
        for (TipoPaciente t : TipoPaciente.values()) {
            assertTrue(OperacionesConjuntos.esSubconjunto(t.sintomasDeRiesgo(), CatalogoClinico.UNIVERSO),
                    t.nombre() + " debe cumplir G ⊆ U");
        }
        assertTrue(TipoPaciente.ADULTO.sintomasDeRiesgo().isEmpty());
    }
}
