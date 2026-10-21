package com.medset;

import com.medset.util.SceneManager;
import com.medset.util.Vista;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MedSetApplication extends Application {

    private static final String TITULO = "MedSet · Teoría de conjuntos aplicada al triage";
    private static final String ICONO = "/com/medset/images/medset-icon.png";
    private static final String SMOKE_TEST = "--smoke-test";

    @Override
    public void start(Stage stage) throws IOException {
        boolean smokeTest = getParameters().getRaw().contains(SMOKE_TEST);
        if (smokeTest) {
            // Verifica que todas las vistas carguen sin errores.
            for (Vista vista : Vista.values()) {
                SceneManager.cargar(vista);
            }
        }

        stage.setTitle(TITULO);
        stage.getIcons().add(new Image(MedSetApplication.class.getResource(ICONO).toExternalForm()));
        stage.setScene(new Scene(SceneManager.cargar(Vista.INICIO), 1200, 750));
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();

        if (smokeTest) {
            Platform.runLater(stage::close);
        }
    }
}
