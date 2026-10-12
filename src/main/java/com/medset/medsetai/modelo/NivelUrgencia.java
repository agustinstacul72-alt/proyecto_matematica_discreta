package com.medset.medsetai.modelo;

/**
 * Niveles de prioridad del triage, ordenados de menor a mayor.
 * El orden permite comparar niveles (relación de orden total).
 */
public enum NivelUrgencia {
    BAJA("Baja", "Puede esperar; control en consulta general."),
    MEDIA("Media", "Atención en las próximas horas."),
    ALTA("Alta", "Atención inmediata.");

    private final String etiqueta;
    private final String indicacion;

    NivelUrgencia(String etiqueta, String indicacion) {
        this.etiqueta = etiqueta;
        this.indicacion = indicacion;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public String indicacion() {
        return indicacion;
    }

    /** Devuelve el mayor de los dos niveles. */
    public NivelUrgencia max(NivelUrgencia otro) {
        return this.compareTo(otro) >= 0 ? this : otro;
    }
}
