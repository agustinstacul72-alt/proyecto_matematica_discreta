package com.medset.modelo;

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

    /** El nivel siguiente (BAJA → MEDIA → ALTA). ALTA se queda en ALTA. */
    public NivelUrgencia subir() {
        return this == ALTA ? ALTA : values()[ordinal() + 1];
    }
}
