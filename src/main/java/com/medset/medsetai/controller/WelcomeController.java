package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles navigation actions from the welcome screen.
 */
public class WelcomeController {

    /**
     * Opens the dashboard in the window that fired the action.
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

        System.out.println("Entrando a MedSet AI...");
    }

    /**
     * Opens the project information screen.
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

        System.out.println("Abriendo información del proyecto...");
    }
}