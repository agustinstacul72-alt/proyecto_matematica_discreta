package com.medset.medsetai.controller;

import com.medset.medsetai.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;


public class WelcomeController {


    @FXML
    private void handleStart(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "dashboard-view.fxml");

    }

    @FXML
    private void handleLaboratorio(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "laboratorio-view.fxml");
    }


    @FXML
    private void handleJuego(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage, "juego-view.fxml");
    }

    @FXML
    private void handleCredits(ActionEvent event) throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(stage, "credits-view.fxml");

    }
}