package com.medset.controller;

import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class CreditsController {

    @FXML
    private void handleClose(ActionEvent event) throws IOException {
        SceneManager.irA(event, Vista.INICIO);
    }
}
