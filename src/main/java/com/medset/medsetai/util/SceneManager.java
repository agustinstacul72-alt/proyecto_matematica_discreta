package com.medset.medsetai.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Loads an application FXML view and displays it in an existing stage.
 */
public class SceneManager {

    /**
     * Replaces the stage's current scene with the named view.
     *
     * @param stage window in which to display the view
     * @param fxmlFile FXML filename under the application's {@code view} resource
     *                 directory
     * @throws IOException if the FXML resource cannot be loaded
     */
    public static void switchTo(Stage stage, String fxmlFile) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(
                        "/com/medset/medsetai/view/" + fxmlFile
                )
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 750);

        stage.setScene(scene);
        stage.show();
    }
}