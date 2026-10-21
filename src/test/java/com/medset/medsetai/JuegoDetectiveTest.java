package com.medset.medsetai;

import com.medset.medsetai.modelo.CatalogoClinico;
import com.medset.medsetai.modelo.Enfermedad;
import com.medset.medsetai.modelo.JuegoDetective;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JuegoDetectiveTest {

    private final JuegoDetective juego = new JuegoDetective(CatalogoClinico.ENFERMEDADES, new Random(1));

    @Test
    void preguntarFiltraCandidatosPorPertenencia() {
        Enfermedad neumonia = CatalogoClinico.ENFERMEDADES.get(6);
        juego.iniciarCon(neumonia);
        assertTrue(juego.preguntar("dolor de pecho"));
        assertEquals(1, juego.candidatos().size()); // solo la neumonía tiene dolor de pecho
        assertEquals(neumonia, juego.candidatos().get(0));
        assertEquals(JuegoDetective.PUNTOS_INICIALES - JuegoDetective.COSTO_PREGUNTA, juego.puntos());
    }

    @Test
    void respuestaNegativaTambienFiltra() {
        juego.iniciarCon(CatalogoClinico.ENFERMEDADES.get(1)); // alergia
        assertFalse(juego.preguntar("fiebre"));
        for (Enfermedad e : juego.candidatos()) {
            assertFalse(e.sintomas().contains("fiebre"));
        }
    }

    @Test
    void siguiendoLaPistaSiempreSeLlegaAUnCandidato() {
        for (Enfermedad secreta : CatalogoClinico.ENFERMEDADES) {
            juego.iniciarCon(secreta);
            int preguntas = 0;
            while (juego.candidatos().size() > 1) {
                String pista = juego.mejorPregunta();
                assertNotNull(pista);
                juego.preguntar(pista);
                preguntas++;
            }
            assertEquals(secreta, juego.candidatos().get(0));
            assertTrue(preguntas <= 4, "Con 7 enfermedades bastan pocas preguntas");
            assertTrue(juego.adivinar(secreta));
            assertTrue(juego.ganado());
        }
    }

    @Test
    void errorDescuentaPuntosYNoSeRepitenPreguntas() {
        juego.iniciarCon(CatalogoClinico.ENFERMEDADES.get(0));
        assertFalse(juego.adivinar(CatalogoClinico.ENFERMEDADES.get(3)));
        assertEquals(JuegoDetective.PUNTOS_INICIALES - JuegoDetective.COSTO_ERROR, juego.puntos());
        juego.preguntar("tos");
        assertThrows(IllegalArgumentException.class, () -> juego.preguntar("tos"));
    }
}
