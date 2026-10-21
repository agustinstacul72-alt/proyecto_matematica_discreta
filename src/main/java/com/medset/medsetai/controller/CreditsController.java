package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class CreditsController {

    @FXML
    private void handleClose(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "welcome-view.fxml");
        System.out.println("Volviendo a la vista principal");
    }
}