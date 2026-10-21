package com.medset.controller;

import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private void handleStart(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.CALCULADORA);
    }

    @FXML
    private void handleLaboratorio(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.TRIAGE);
    }

    @FXML
    private void handleJuego(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.JUEGO);
    }

    @FXML
    private void handleCredits(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.CREDITOS);
    }
}
