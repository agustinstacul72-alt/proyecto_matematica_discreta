package com.medset.medsetai;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Configura y muestra la ventana principal de MedSet.
 */
public class MedSetApplication extends Application {

    /**
     * Loads the welcome view and displays the application stage.
     *
     * @param stage primary JavaFX window
     * @throws IOException if the welcome FXML resource cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {

        boolean smokeTest = getParameters().getRaw().contains("--smoke-test");
        if (smokeTest) {
            FXMLLoader.load(MedSetApplication.class.getResource(
                    "/com/medset/medsetai/view/dashboard-view.fxml"
            ));
            for (String vista : new String[]{"credits-view.fxml", "laboratorio-view.fxml", "juego-view.fxml"}) {
                FXMLLoader.load(MedSetApplication.class.getResource("/com/medset/medsetai/view/" + vista));
            }
        }

        FXMLLoader loader = new FXMLLoader(
                MedSetApplication.class.getResource(
                        "/com/medset/medsetai/view/welcome-view.fxml"
                )
        );

        Scene scene = new Scene(loader.load(), 1200, 750);

        stage.setTitle("MedSet · Teoría de conjuntos aplicada al triage");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();

        if (smokeTest) {
            Platform.runLater(stage::close);
        }
    }
}