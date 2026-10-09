package com.medset.medsetai.controller;

import com.medset.medsetai.model.Patient;
import com.medset.medsetai.repository.DatabaseManager;
import com.medset.medsetai.repository.PatientRepository;
import com.medset.medsetai.service.PatientSelectionContext;
import com.medset.medsetai.service.PatientService;
import com.medset.medsetai.service.SymptomNormalizer;
import com.medset.medsetai.util.SceneManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * Controls the patient management screen.
 *
 * <p>The controller connects JavaFX controls with {@link PatientService}.
 * It supports creating, editing, deleting, listing, and selecting patients
 * for educational set-theory analysis.</p>
 *
 * <p>Operations that may call the AI provider are performed asynchronously
 * to keep the JavaFX application responsive.</p>
 */
public class PatientController {

    /** Input field for the patient's name. */
    @FXML
    private TextField patientNameField;

    /** Input field for the patient's age. */
    @FXML
    private TextField patientAgeField;

    /** Text area containing the original symptom descriptions. */
    @FXML
    private TextArea symptomsArea;

    /** List displaying patients stored in the database. */
    @FXML
    private ListView<Patient> patientListView;

    /** Displays operation status and validation messages. */
    @FXML
    private Label statusLabel;

    /** Button disabled while a patient is being saved. */
    @FXML
    private Button savePatientButton;

    /** Button used to submit selected patients for analysis. */
    @FXML
    private Button prepareAnalysisButton;

    /** Service that performs patient application operations. */
    private PatientService patientService;

    /** Indicates whether the form is currently being edited. */
    private Long editingPatientId;

    /**
     * Initializes the screen after JavaFX loads the FXML file.
     *
     * <p>This method creates the required services, configures multiple
     * selection, and loads existing patient records.</p>
     */
    @FXML
    public void initialize() {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initializeDatabase();

        PatientRepository repository = new PatientRepository(databaseManager);
        SymptomNormalizer normalizer = new SymptomNormalizer();

        patientService = new PatientService(repository, normalizer);

        patientListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);

        patientListView.getSelectionModel()
                .getSelectedItems()
                .addListener((javafx.collections.ListChangeListener<Patient>) change -> {
                    List<Patient> selected = patientListView
                            .getSelectionModel()
                            .getSelectedItems();

                    if (selected.size() == 1) {
                        loadPatientIntoForm(selected.get(0));
                    }
                });

        refreshPatients();
        statusLabel.setText("Ready.");
    }

    /**
     * Refreshes the list using the latest records in SQLite.
     */
    private void refreshPatients() {
        List<Patient> patients = patientService.getAllPatients();

        patientListView.setItems(
                FXCollections.observableArrayList(patients)
        );
    }

    /**
     * Clears the form so that the user can create a new patient.
     */
    @FXML
    private void handleNewPatient() {
        patientListView.getSelectionModel().clearSelection();
        clearForm();
        statusLabel.setText("Enter the new patient's information.");
    }

    /**
     * Creates or updates a patient using the current form values.
     *
     * <p>Validation and symptom normalization are performed by the service.
     * The AI request runs in a background task; UI changes return to the
     * JavaFX application thread.</p>
     */
    @FXML
    private void handleSavePatient() {
        String name = patientNameField.getText();
        String ageText = patientAgeField.getText();
        String symptomsText = symptomsArea.getText();

        final int age;

        try {
            age = Integer.parseInt(ageText == null ? "" : ageText.trim());
        } catch (NumberFormatException exception) {
            showError("Please enter a valid age.");
            return;
        }

        savePatientButton.setDisable(true);
        prepareAnalysisButton.setDisable(true);
        statusLabel.setText("Processing symptoms with Ollama...");

        CompletableFuture
                .supplyAsync(() -> {
                    if (editingPatientId == null) {
                        return patientService.createPatient(
                                name,
                                age,
                                symptomsText
                        );
                    }

                    return patientService.updatePatient(
                            editingPatientId,
                            name,
                            age,
                            symptomsText
                    );
                })
                .whenComplete((patient, error) -> Platform.runLater(() -> {
                    savePatientButton.setDisable(false);
                    prepareAnalysisButton.setDisable(false);

                    if (error != null) {
                        statusLabel.setText("Could not save patient.");
                        showError(getErrorMessage(error));
                        return;
                    }

                    refreshPatients();
                    clearForm();

                    statusLabel.setText(
                            "Patient saved successfully: " + patient.getName()
                    );
                }));
    }

    /**
     * Deletes all patients currently selected in the list.
     *
     * <p>The database repository also deletes their associated symptoms.</p>
     */
    @FXML
    private void handleDeleteSelected() {
        List<Patient> selected = List.copyOf(
                patientListView.getSelectionModel().getSelectedItems()
        );

        if (selected.isEmpty()) {
            showError("Select at least one patient to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete patients");
        confirmation.setHeaderText("Delete selected patients?");
        confirmation.setContentText(
                "This will also delete their stored symptoms."
        );

        if (confirmation.showAndWait().orElse(
                javafx.scene.control.ButtonType.CANCEL
        ) != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            for (Patient patient : selected) {
                patientService.deletePatient(patient.getId());
            }

            refreshPatients();
            clearForm();
            statusLabel.setText("Selected patients deleted.");

        } catch (RuntimeException exception) {
            showError(getErrorMessage(exception));
        }
    }

    /**
     * Publishes the selected patients to the analysis module.
     *
     * <p>The selection is stored in {@link PatientSelectionContext}. This
     * method does not perform set operations or generate the report itself.</p>
     */
    @FXML
    private void handleAnalyzeSelected() {
        List<Patient> selected = List.copyOf(
                patientListView.getSelectionModel().getSelectedItems()
        );

        if (selected.isEmpty()) {
            showError("Select at least one patient for analysis.");
            return;
        }

        PatientSelectionContext.setSelectedPatients(selected);

        statusLabel.setText(
                selected.size() + " patient(s) prepared for analysis."
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Analysis prepared");
        alert.setHeaderText("Patients are ready for analysis");
        alert.setContentText(
                "The selected patients are available through "
                        + "PatientSelectionContext for the analysis module."
        );
        alert.showAndWait();
    }

    /**
     * Returns to the main dashboard.
     *
     * @throws IllegalStateException if the dashboard cannot be loaded
     */
    @FXML
    private void handleBackToDashboard() {
        try {
            Stage stage = (Stage) patientListView.getScene().getWindow();
            SceneManager.switchTo(stage, "dashboard-view.fxml");

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not open the dashboard.",
                    exception
            );
        }
    }

    /**
     * Loads an existing patient's data into the form.
     *
     * <p>Because the same original text can be stored alongside multiple
     * normalized symptoms, duplicate original descriptions are removed
     * before they are displayed.</p>
     *
     * @param patient patient to display
     */
    private void loadPatientIntoForm(Patient patient) {
        editingPatientId = patient.getId();

        patientNameField.setText(patient.getName());
        patientAgeField.setText(String.valueOf(patient.getAge()));

        String originalText = patient.getSymptoms()
                .stream()
                .map(symptom -> symptom.getOriginalText())
                .filter(text -> text != null && !text.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        symptomsArea.setText(originalText);

        statusLabel.setText(
                "Editing patient: " + patient.getName()
        );
    }

    /**
     * Resets the form and removes the current editing identifier.
     */
    private void clearForm() {
        editingPatientId = null;

        patientNameField.clear();
        patientAgeField.clear();
        symptomsArea.clear();

        patientListView.getSelectionModel().clearSelection();
    }

    /**
     * Displays an error message in a JavaFX dialog.
     *
     * @param message explanation to show to the user
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("MedSet AI");
        alert.setHeaderText("Operation could not be completed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Extracts a readable message from an exception or asynchronous failure.
     *
     * @param error exception received by the controller
     * @return readable error description
     */
    private String getErrorMessage(Throwable error) {
        Throwable cause = error;

        while ((cause instanceof CompletionException
                || cause.getCause() != null)
                && cause.getCause() != null) {
            cause = cause.getCause();
        }

        if (cause.getMessage() == null || cause.getMessage().isBlank()) {
            return "An unexpected error occurred.";
        }

        return cause.getMessage();
    }

    /**
     * Opens the report view for the prepared patient analysis.
     */
    @FXML
    private void handleShowReport() {
        if (PatientSelectionContext.getSelectedPatients().isEmpty()) {
            showError("Please prepare an analysis first.");
            return;
        }

        // TODO: Navigate to the report view when it is implemented.
        showError("The report view has not been implemented yet.");
    }

    /**
     * Opens the graph view for the prepared patient analysis.
     */
    @FXML
    private void handleShowGraph() {
        if (PatientSelectionContext.getSelectedPatients().isEmpty()) {
            showError("Please prepare an analysis first.");
            return;
        }

        // TODO: Navigate to the graph view when it is implemented.
        showError("The graph view has not been implemented yet.");
    }
}