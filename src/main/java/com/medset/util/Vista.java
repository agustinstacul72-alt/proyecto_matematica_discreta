package com.medset.util;

/**
 * Pantallas de la aplicación y el archivo FXML que define cada una.
 */
public enum Vista {
    INICIO("welcome-view.fxml"),
    CALCULADORA("dashboard-view.fxml"),
    TRIAGE("laboratorio-view.fxml"),
    JUEGO("juego-view.fxml"),
    CREDITOS("credits-view.fxml");

    private final String archivo;

    Vista(String archivo) {
        this.archivo = archivo;
    }

    String archivo() {
        return archivo;
    }
}
