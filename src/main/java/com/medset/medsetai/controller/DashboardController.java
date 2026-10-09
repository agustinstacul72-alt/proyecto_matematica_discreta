package com.medset.medsetai.controller;

import com.medset.medsetai.service.AiService;
import com.medset.medsetai.service.OllamaProvider;
import com.medset.medsetai.util.AppConfig;
import com.medset.medsetai.util.ChatMarkdownRenderer;
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
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

/**
 * Connects the dashboard views to Ollama model selection, chat, and patient
 * workflow actions.
 *
 * <p>Patient report and graph actions are placeholders; they do not perform
 * analysis or render a chart yet.</p>
 */
public class DashboardController {

    private static final String GEMMA_MODEL_OPTION = "Gemma 3 4B (Ollama local)";
    private static final String NEMOTRON_MODEL_OPTION = "Nemotron 3 Nano 4B (Ollama local)";

    @FXML
    private VBox assistantPanel;

    @FXML
    private VBox patientPanel;

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private ComboBox<String> assistantModelComboBox;

    @FXML
    private ComboBox<String> patientModelComboBox;

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

        assistantModelComboBox.setItems(
                FXCollections.observableArrayList(
                        GEMMA_MODEL_OPTION,
                        NEMOTRON_MODEL_OPTION
                )
        );

        patientModelComboBox.setItems(
                FXCollections.observableArrayList(
                        GEMMA_MODEL_OPTION,
                        NEMOTRON_MODEL_OPTION
                )
        );

        assistantModelComboBox.setValue(NEMOTRON_MODEL_OPTION);
        patientModelComboBox.setValue(NEMOTRON_MODEL_OPTION);

        aiService = new AiService(
                createProvider(NEMOTRON_MODEL_OPTION)
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
     * Opens the patient management screen.
     *
     * @throws IllegalStateException if the patient screen cannot be loaded
     */
    @FXML
    private void showPatient() {
        try {
            Stage stage = (Stage) patientPanel.getScene().getWindow();

            SceneManager.switchTo(stage, "patient-view.fxml");

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not open patient management.",
                    exception
            );
        }
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
     * Replaces the active provider according to the selected Ollama model.
     *
     * @param modelOption selected Ollama model name
     */
    private void updateProvider(String modelOption) {
        aiService.setProvider(createProvider(modelOption));
    }

    private OllamaProvider createProvider(String modelOption) {
        if (NEMOTRON_MODEL_OPTION.equals(modelOption)) {
            return new OllamaProvider(
                    AppConfig.getOllamaNemotronModel(),
                    "Ollama · Nemotron 3 Nano 4B"
            );
        }
        return new OllamaProvider(
                AppConfig.getOllamaModel(),
                "Ollama · Gemma 3 4B"
        );
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

        String selectedModelOption = assistantModelComboBox.getValue();

        addUserMessage(message);

        messageField.clear();

        String finalMessage = message;
        String assistantPrompt = """
                Responde en el mismo idioma que la pregunta. Da formato legible
                con Markdown: usa **negrita** para conceptos importantes, títulos
                con # y listas cuando ayuden a ordenar la explicación. No muestres
                las marcas de Markdown como si fueran texto literal.

                Para matemáticas, escribe símbolos Unicode legibles cuando sea
                posible, por ejemplo A ∪ B, A ∩ B, x ∈ A, x ∉ A, A ⊆ B, ∅,
                ℕ, ℤ y ℝ. Si necesitas usar LaTeX, delimita las expresiones
                matemáticas con $...$ o \\(...\\).

                Pregunta:
                %s
                """.formatted(finalMessage);

        VBox responseContent = addAiMessage(
                "Pensando...",
                selectedModelOption
        );
        StringBuilder responseText = new StringBuilder();

        CompletableFuture
                .supplyAsync(() -> {
                    AiService requestService = new AiService(
                            createProvider(selectedModelOption)
                    );
                    return requestService.ask(assistantPrompt, fragment ->
                            Platform.runLater(() -> {
                                responseText.append(fragment);
                                ChatMarkdownRenderer.render(
                                        responseContent,
                                        responseText.toString()
                                );
                                chatScrollPane.setVvalue(1.0);
                            })
                    );
                })
                .exceptionally(error -> {
                    Throwable cause = error;
                    while (cause instanceof CompletionException
                            && cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    String errorMessage = cause.getMessage() == null
                            ? cause.getClass().getSimpleName()
                            : cause.getMessage();
                    Platform.runLater(() -> {
                        if (responseText.isEmpty()) {
                            ChatMarkdownRenderer.render(
                                    responseContent,
                                    "No pude obtener una respuesta. " + errorMessage
                            );
                        } else {
                            ChatMarkdownRenderer.render(
                                    responseContent,
                                    responseText + "\n\nNo pude completar la respuesta. "
                                            + errorMessage
                            );
                        }
                        chatScrollPane.setVvalue(1.0);
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
        messageBox.setMaxWidth(Double.MAX_VALUE);

        messageBox.getStyleClass()
                .add("user-message");

        Label sender = new Label("You");

        sender.getStyleClass()
                .add("message-sender");

        Label text = new Label(message);

        text.setWrapText(true);
        text.setMaxWidth(Double.MAX_VALUE);

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
    private VBox addAiMessage(String message, String modelName) {

        VBox messageBox = new VBox(4);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        messageBox.getStyleClass()
                .add("ai-message");

        Label sender = new Label(modelName);

        sender.getStyleClass()
                .add("message-sender");

        VBox text = new VBox(7);
        text.getStyleClass().add("message-content");
        text.setMaxWidth(Double.MAX_VALUE);
        ChatMarkdownRenderer.render(text, message);

        messageBox.getChildren()
                .addAll(sender, text);

        chatContainer.getChildren()
                .add(messageBox);

        chatScrollPane.setVvalue(1.0);
        return text;
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
                patientModelComboBox.getValue();

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