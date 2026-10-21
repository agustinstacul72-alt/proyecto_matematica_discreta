package com.medset.logica;

import java.util.Set;

/**
 * Lo que muestra la calculadora después de operar: el resultado, el detalle y
 * cómo dibujar el diagrama de Venn.
 *
 * @param resultado texto principal, por ejemplo "A ∪ B = {1, 2, 3}"
 * @param detalle   cardinalidades y explicación
 * @param diagrama  diagrama de Venn, o {@code null} si no se debe redibujar
 */
public record ResultadoOperacion(String resultado, String detalle, Diagrama diagrama) {

    /**
     * Diagrama de Venn a dibujar. Si {@code universo} no es {@code null}, es el
     * diagrama del complemento de A en U; si no, el de A y B con zonas resaltadas.
     */
    public record Diagrama(Set<String> a, Set<String> b, Set<String> universo,
                           Set<ZonaVenn> resaltar, boolean alerta) {

        public boolean esComplemento() {
            return universo != null;
        }
    }
}
