package com.medset.medsetai.controller;

import com.medset.medsetai.service.AiService;
import com.medset.medsetai.service.GeminiProvider;
import com.medset.medsetai.service.OllamaProvider;
import com.medset.medsetai.util.SceneManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Connects the dashboard views to AI chat, provider selection, and patient
 * workflow actions.
 *
 * <p>Patient report and graph actions are placeholders; they do not perform
 * analysis or render a chart yet.</p>
 */
public class DashboardController {

    @FXML
    private VBox assistantPanel;

    @FXML
    private VBox patientPanel;

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private ComboBox<String> assistantProviderComboBox;

    @FXML
    private ComboBox<String> patientProviderComboBox;

    @FXML
    private TextField messageField;

    @FXML
    private TextField patientNameField;

    @FXML
    private TextField patientAgeField;

    @FXML
    private TextArea symptomsArea;


    private AiService aiService;


    /**
     * Populates provider selectors and defaults the service to Ollama.
     */
    @FXML
    private void initialize() {

        assistantProviderComboBox.setItems(
                FXCollections.observableArrayList(
                        "Ollama",
                        "Gemini"
                )
        );

        patientProviderComboBox.setItems(
                FXCollections.observableArrayList(
                        "Ollama",
                        "Gemini"
                )
        );

        assistantProviderComboBox.setValue("Ollama");
        patientProviderComboBox.setValue("Ollama");

        aiService = new AiService(
                new OllamaProvider()
        );
    }


    /**
     * Displays the assistant panel and hides the patient panel.
     */
    @FXML
    private void showAssistant() {

        assistantPanel.setVisible(true);
        assistantPanel.setManaged(true);

        patientPanel.setVisible(false);
        patientPanel.setManaged(false);
    }

    /**
     * Displays the patient panel and hides the assistant panel.
     */
    @FXML
    private void showPatient() {

        assistantPanel.setVisible(false);
        assistantPanel.setManaged(false);

        patientPanel.setVisible(true);
        patientPanel.setManaged(true);
    }

    /**
     * Returns to the welcome screen in the current window.
     *
     * @param event action fired by the back button
     * @throws IOException if the welcome view cannot be loaded
     */
    @FXML
    private void handleBackToWelcome(ActionEvent event)
            throws IOException {

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        SceneManager.switchTo(
                stage,
                "welcome-view.fxml"
        );
    }


    /**
     * Replaces the active provider according to the selected display name.
     *
     * @param providerName selected provider name; names other than {@code Gemini}
     *                     select Ollama
     */
    private void updateProvider(String providerName) {

        if ("Gemini".equals(providerName)) {

            aiService.setProvider(
                    new GeminiProvider()
            );

        } else {

            aiService.setProvider(
                    new OllamaProvider()
            );
        }
    }


    /**
     * Sends the non-empty chat prompt on a background thread and posts the
     * response or error back to the JavaFX application thread.
     */
    @FXML
    private void handleSendMessage() {

        String message = messageField.getText();

        if (message == null || message.isBlank()) {
            return;
        }

        message = message.trim();

        String selectedProvider =
                assistantProviderComboBox.getValue();

        updateProvider(selectedProvider);

        addUserMessage(message);

        messageField.clear();

        String finalMessage = message;

        addAiMessage("Pensando...");

        CompletableFuture
                .supplyAsync(() ->
                        aiService.ask(finalMessage)
                )
                .thenAccept(response ->
                        Platform.runLater(() -> {

                            removeLastMessage();

                            addAiMessage(response);

                            chatScrollPane.setVvalue(1.0);
                        })
                )
                .exceptionally(error -> {

                    Platform.runLater(() -> {

                        removeLastMessage();

                        addAiMessage(
                                "No pude obtener una respuesta. "
                                        + error.getMessage()
                        );
                    });

                    return null;
                });
    }


    /**
     * Adds a user-authored message to the chat transcript.
     *
     * @param message text entered by the user
     */
    private void addUserMessage(String message) {

        VBox messageBox = new VBox(4);

        messageBox.getStyleClass()
                .add("user-message");

        Label sender = new Label("You");

        sender.getStyleClass()
                .add("message-sender");

        Label text = new Label(message);

        text.setWrapText(true);

        text.getStyleClass()
                .add("message-text");

        messageBox.getChildren()
                .addAll(sender, text);

        chatContainer.getChildren()
                .add(messageBox);

        chatScrollPane.setVvalue(1.0);
    }


    /**
     * Adds a provider-authored message to the chat transcript.
     *
     * @param message response or status text to display
     */
    private void addAiMessage(String message) {

        VBox messageBox = new VBox(4);

        messageBox.getStyleClass()
                .add("ai-message");

        Label sender = new Label(
                aiService.getProviderName()
        );

        sender.getStyleClass()
                .add("message-sender");

        Label text = new Label(message);

        text.setWrapText(true);

        text.getStyleClass()
                .add("message-text");

        messageBox.getChildren()
                .addAll(sender, text);

        chatContainer.getChildren()
                .add(messageBox);

        chatScrollPane.setVvalue(1.0);
    }


    /**
     * Removes the last transcript item, used to replace the pending indicator.
     */
    private void removeLastMessage() {

        int size = chatContainer
                .getChildren()
                .size();

        if (size > 1) {

            chatContainer
                    .getChildren()
                    .remove(size - 1);
        }
    }


    /**
     * Reads patient form fields and logs the request; analysis is not yet
     * implemented.
     */
    @FXML
    private void handleShowReport() {

        String name = patientNameField.getText();
        String age = patientAgeField.getText();
        String symptoms = symptomsArea.getText();

        if (name == null || name.isBlank()) {
            name = "Unnamed patient";
        }

        if (age == null || age.isBlank()) {
            age = "Not specified";
        }

        if (symptoms == null || symptoms.isBlank()) {
            symptoms = "No symptoms entered.";
        }

        String provider =
                patientProviderComboBox.getValue();

        updateProvider(provider);

        System.out.println(
                "Patient analysis requested"
        );

        System.out.println(
                "Name: " + name
        );

        System.out.println(
                "Age: " + age
        );

        System.out.println(
                "Symptoms: " + symptoms
        );

        /*
         * Próximo paso:
         *
         * 1. Crear AnalysisResult
         * 2. Enviar síntomas a la IA
         * 3. Recibir patrón estructurado
         * 4. Crear los conjuntos
         * 5. Mostrar el informe
         */
    }

    /**
     * Logs a graph request; graph generation is not yet implemented.
     */
    @FXML
    private void handleGraph() {

        System.out.println(
                "Graph requested"
        );

        /*
         * Próximo paso:
         *
         * AnalysisResult
         *        ↓
         * SetOperations
         *        ↓
         * JavaFX graph
         */
    }
}