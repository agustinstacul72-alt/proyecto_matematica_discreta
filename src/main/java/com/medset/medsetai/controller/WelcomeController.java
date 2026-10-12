package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Navegación desde la pantalla de inicio hacia los tres módulos y los créditos.
 */
public class WelcomeController {

    /**
     * Abre la calculadora de conjuntos.
     *
     * @param event action fired by the start button
     * @throws IOException if the dashboard view cannot be loaded
     */
    @FXML
    private void handleStart(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "dashboard-view.fxml");

    }

    /** Abre el módulo de triage con conjuntos. */
    @FXML
    private void handleLaboratorio(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "laboratorio-view.fxml");
    }

    /** Abre el juego "Detective de síntomas". */
    @FXML
    private void handleJuego(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "juego-view.fxml");
    }

    /**
     * Abre la pantalla de integrantes.
     *
     * @param event action fired by the project information button
     * @throws IOException if the credits view cannot be loaded
     */
    @FXML
    private void handleCredits(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "credits-view.fxml");

    }
}