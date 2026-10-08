package com.medset.medsetai;

import javafx.application.Application;

/**
 * Command-line entry point that launches the JavaFX application.
 */
public class Launcher {

    /**
     * Delegates application startup to JavaFX.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(MedSetApplication.class, args);
    }
}