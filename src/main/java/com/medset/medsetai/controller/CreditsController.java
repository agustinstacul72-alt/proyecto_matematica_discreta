package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles navigation actions from the project information screen.
 */
public class CreditsController {

    /**
     * Returns to the welcome screen in the current window.
     *
     * @param event action fired by the close button
     * @throws IOException if the welcome view cannot be loaded
     */
    @FXML
    private void handleClose(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "welcome-view.fxml");
        System.out.println("Volviendo a la vista principal");
    }
}