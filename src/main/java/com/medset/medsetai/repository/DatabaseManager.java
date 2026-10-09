package com.medset.medsetai.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the local SQLite database used by MedSet AI.
 *
 * <p>The database file is created inside the user's home directory under
 * {@code .medset-ai/medset.db}. The class also creates the required tables
 * and configures foreign-key enforcement for each connection.</p>
 */
public class DatabaseManager {

    /** JDBC URL used to connect to the local SQLite database. */
    private final String databaseUrl;

    /**
     * Creates the database manager and ensures that its directory exists.
     *
     * @throws IllegalStateException if the database directory cannot be created
     */
    public DatabaseManager() {
        try {
            Path directory = Path.of(
                    System.getProperty("user.home"),
                    ".medset-ai"
            );

            Files.createDirectories(directory);

            Path databasePath = directory.resolve("medset.db");

            databaseUrl = "jdbc:sqlite:"
                    + databasePath.toAbsolutePath()
                    .toString()
                    .replace("\\", "/");

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not create the MedSet AI database directory.",
                    exception
            );
        }
    }

    /**
     * Opens a connection to the local SQLite database.
     *
     * <p>Foreign-key enforcement is enabled on every connection because
     * SQLite applies this setting per connection.</p>
     *
     * @return an open database connection
     * @throws SQLException if a connection cannot be established
     */
    public Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    /**
     * Creates the patient and symptom tables when they do not already exist.
     *
     * <p>The symptom table references the patient table. Deleting a patient
     * therefore also deletes its associated symptoms.</p>
     *
     * @throws IllegalStateException if the schema cannot be initialized
     */
    public void initializeDatabase() {
        String createPatients = """
                CREATE TABLE IF NOT EXISTS patients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER NOT NULL CHECK(age >= 0),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String createSymptoms = """
                CREATE TABLE IF NOT EXISTS symptoms (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    original_text TEXT NOT NULL,
                    normalized_name TEXT NOT NULL,
                    language TEXT NOT NULL,
                    FOREIGN KEY (patient_id)
                        REFERENCES patients(id)
                        ON DELETE CASCADE
                )
                """;

        try (
                Connection connection = connect();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(createPatients);
            statement.execute(createSymptoms);

            System.out.println("MedSet AI SQLite database initialized.");

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not initialize the MedSet AI database.",
                    exception
            );
        }
    }
}