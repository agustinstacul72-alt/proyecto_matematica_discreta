package com.medset.medsetai.service;

import com.medset.medsetai.model.Patient;
import com.medset.medsetai.model.Symptom;
import com.medset.medsetai.repository.PatientRepository;

import java.util.List;

/**
 * Implements application-level operations for simulated patients.
 *
 * <p>This service validates patient information, normalizes symptom
 * descriptions, and delegates persistence to {@link PatientRepository}.</p>
 */
public class PatientService {

    /** Repository responsible for storing patient data. */
    private final PatientRepository patientRepository;

    /** Component responsible for normalizing symptom text. */
    private final SymptomNormalizer symptomNormalizer;

    /**
     * Creates the patient service and its required dependencies.
     *
     * @param patientRepository repository used for persistence
     * @param symptomNormalizer service used to normalize symptom text
     */
    public PatientService(
            PatientRepository patientRepository,
            SymptomNormalizer symptomNormalizer
    ) {
        this.patientRepository = patientRepository;
        this.symptomNormalizer = symptomNormalizer;
    }

    /**
     * Validates and creates a patient with normalized symptoms.
     *
     * @param name patient's name
     * @param age patient's age
     * @param symptomsText free-text symptoms entered by the user
     * @return saved patient
     */
    public Patient createPatient(
            String name,
            int age,
            String symptomsText
    ) {
        validatePatient(name, age, symptomsText);

        List<Symptom> symptoms = symptomNormalizer.normalize(symptomsText);

        Patient patient = new Patient(name.trim(), age);
        patient.setSymptoms(symptoms);

        return patientRepository.save(patient);
    }

    /**
     * Validates and updates an existing patient.
     *
     * @param id identifier of the patient to update
     * @param name updated patient name
     * @param age updated age
     * @param symptomsText updated symptom descriptions
     * @return updated patient
     * @throws IllegalArgumentException if the patient does not exist
     */
    public Patient updatePatient(
            long id,
            String name,
            int age,
            String symptomsText
    ) {
        validatePatient(name, age, symptomsText);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Patient not found."
                ));

        List<Symptom> symptoms = symptomNormalizer.normalize(symptomsText);

        patient.setName(name.trim());
        patient.setAge(age);
        patient.setSymptoms(symptoms);

        return patientRepository.update(patient);
    }

    /**
     * Returns all saved patients and their symptoms.
     *
     * @return list of saved patients
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Deletes a patient and its associated symptoms.
     *
     * @param id identifier of the patient to delete
     * @return true if the patient existed and was deleted
     */
    public boolean deletePatient(long id) {
        return patientRepository.delete(id);
    }

    /**
     * Validates the fields required to create or update a patient.
     *
     * @param name patient's name
     * @param age patient's age
     * @param symptomsText symptom descriptions
     * @throws IllegalArgumentException if a field is invalid
     */
    private void validatePatient(
            String name,
            int age,
            String symptomsText
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter the patient's name."
            );
        }

        if (age < 0 || age > 130) {
            throw new IllegalArgumentException(
                    "Age must be between 0 and 130."
            );
        }

        if (symptomsText == null || symptomsText.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter at least one symptom."
            );
        }
    }
}