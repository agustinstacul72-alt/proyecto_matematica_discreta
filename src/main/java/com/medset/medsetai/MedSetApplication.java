package com.medset.medsetai;

import com.medset.medsetai.repository.DatabaseManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point of the MedSet AI JavaFX application.
 *
 * <p>Initializes the local database and displays the welcome screen.
 * An optional smoke-test argument loads important FXML screens to verify
 * that their resources and controllers can be initialized.</p>
 */
public class MedSetApplication extends Application {

    /**
     * Initializes the database and opens the welcome screen.
     *
     * @param stage primary JavaFX application window
     * @throws IOException if an FXML resource cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializeDatabase();

        boolean smokeTest = getParameters()
                .getRaw()
                .contains("--smoke-test");

        if (smokeTest) {
            FXMLLoader.load(
                    MedSetApplication.class.getResource(
                            "/com/medset/medsetai/view/dashboard-view.fxml"
                    )
            );

            FXMLLoader.load(
                    MedSetApplication.class.getResource(
                            "/com/medset/medsetai/view/credits-view.fxml"
                    )
            );

            FXMLLoader.load(
                    MedSetApplication.class.getResource(
                            "/com/medset/medsetai/view/patient-view.fxml"
                    )
            );
        }

        FXMLLoader loader = new FXMLLoader(
                MedSetApplication.class.getResource(
                        "/com/medset/medsetai/view/welcome-view.fxml"
                )
        );

        Scene scene = new Scene(loader.load(), 1200, 750);

        stage.setTitle("MedSet AI");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();

        if (smokeTest) {
            Platform.runLater(stage::close);
        }
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}