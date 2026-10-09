package com.medset.medsetai.repository;

import com.medset.medsetai.model.Patient;
import com.medset.medsetai.model.Symptom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides database operations for patients and their symptoms.
 *
 * <p>This repository separates SQL persistence from the application
 * services and JavaFX controllers. Patient and symptom changes are saved
 * within transactions where multiple database operations must succeed
 * together.</p>
 */
public class PatientRepository {

    /** Database connection manager used by this repository. */
    private final DatabaseManager databaseManager;

    /**
     * Creates a repository using the provided database manager.
     *
     * @param databaseManager manager used to open database connections
     */
    public PatientRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Inserts a new patient and all of its symptoms.
     *
     * <p>The generated patient identifier is assigned to the patient object.
     * If saving any symptom fails, the transaction is rolled back.</p>
     *
     * @param patient patient to save
     * @return the saved patient, including its generated identifier
     * @throws IllegalArgumentException if the patient is null
     * @throws IllegalStateException if the database operation fails
     */
    public Patient save(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null.");
        }

        String sql = """
                INSERT INTO patients (name, age)
                VALUES (?, ?)
                """;

        try (Connection connection = databaseManager.connect()) {
            connection.setAutoCommit(false);

            try {
                long patientId;

                try (
                        PreparedStatement statement = connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
                ) {
                    statement.setString(1, patient.getName());
                    statement.setInt(2, patient.getAge());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not retrieve the patient identifier."
                            );
                        }

                        patientId = keys.getLong(1);
                    }
                }

                patient.setId(patientId);
                insertSymptoms(connection, patient);
                connection.commit();

                return patient;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not save the patient.",
                    exception
            );
        }
    }

    /**
     * Inserts all symptoms associated with a patient.
     *
     * @param connection active database connection
     * @param patient patient whose symptoms will be inserted
     * @throws SQLException if a symptom cannot be inserted
     */
    private void insertSymptoms(
            Connection connection,
            Patient patient
    ) throws SQLException {

        String sql = """
                INSERT INTO symptoms (
                    patient_id,
                    original_text,
                    normalized_name,
                    language
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Symptom symptom : patient.getSymptoms()) {
                statement.setLong(1, patient.getId());
                statement.setString(2, symptom.getOriginalText());
                statement.setString(3, symptom.getNormalizedName());
                statement.setString(4, symptom.getLanguage());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    /**
     * Retrieves every patient together with their symptoms.
     *
     * @return list of stored patients; an empty list if none exist
     * @throws IllegalStateException if the query fails
     */
    public List<Patient> findAll() {
        String sql = """
                SELECT id, name, age
                FROM patients
                ORDER BY id DESC
                """;

        List<Patient> patients = new ArrayList<>();

        try (
                Connection connection = databaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Patient patient = new Patient(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age")
                );

                patient.setSymptoms(
                        findSymptoms(connection, patient.getId())
                );

                patients.add(patient);
            }

            return patients;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not retrieve patients.",
                    exception
            );
        }
    }

    /**
     * Searches for a patient by database identifier.
     *
     * @param id patient identifier
     * @return an optional containing the patient if it exists
     */
    public Optional<Patient> findById(long id) {
        return findAll()
                .stream()
                .filter(patient -> patient.getId() == id)
                .findFirst();
    }

    /**
     * Retrieves all symptoms belonging to a specific patient.
     *
     * @param connection active database connection
     * @param patientId patient identifier
     * @return list of symptoms associated with the patient
     * @throws SQLException if the query fails
     */
    private List<Symptom> findSymptoms(
            Connection connection,
            long patientId
    ) throws SQLException {

        String sql = """
                SELECT id, patient_id, original_text, normalized_name, language
                FROM symptoms
                WHERE patient_id = ?
                ORDER BY id
                """;

        List<Symptom> symptoms = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    symptoms.add(new Symptom(
                            resultSet.getLong("id"),
                            resultSet.getLong("patient_id"),
                            resultSet.getString("original_text"),
                            resultSet.getString("normalized_name"),
                            resultSet.getString("language")
                    ));
                }
            }
        }

        return symptoms;
    }

    /**
     * Updates an existing patient and replaces its symptom records.
     *
     * @param patient patient containing the updated information
     * @return the updated patient
     * @throws IllegalArgumentException if the patient is null
     * @throws IllegalStateException if the patient does not exist or SQL fails
     */
    public Patient update(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null.");
        }

        String updatePatient = """
                UPDATE patients
                SET name = ?, age = ?
                WHERE id = ?
                """;

        String deleteSymptoms = """
                DELETE FROM symptoms
                WHERE patient_id = ?
                """;

        try (Connection connection = databaseManager.connect()) {
            connection.setAutoCommit(false);

            try {
                int updatedRows;

                try (PreparedStatement statement =
                             connection.prepareStatement(updatePatient)) {
                    statement.setString(1, patient.getName());
                    statement.setInt(2, patient.getAge());
                    statement.setLong(3, patient.getId());

                    updatedRows = statement.executeUpdate();
                }

                if (updatedRows == 0) {
                    throw new IllegalStateException(
                            "Patient not found: " + patient.getId()
                    );
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteSymptoms)) {
                    statement.setLong(1, patient.getId());
                    statement.executeUpdate();
                }

                insertSymptoms(connection, patient);
                connection.commit();

                return patient;

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not update the patient.",
                    exception
            );
        }
    }

    /**
     * Deletes a patient by identifier.
     *
     * <p>SQLite's configured foreign-key cascade also deletes the patient's
     * symptom records.</p>
     *
     * @param id identifier of the patient to delete
     * @return true if a patient was deleted; false if no patient matched
     * @throws IllegalStateException if the database operation fails
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM patients WHERE id = ?";

        try (
                Connection connection = databaseManager.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not delete the patient.",
                    exception
            );
        }
    }
}