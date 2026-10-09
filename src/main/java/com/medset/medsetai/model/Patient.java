package com.medset.medsetai.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a simulated patient stored in MedSet AI.
 *
 * <p>A patient contains basic demographic information and a collection of
 * symptoms. This class is a data model and does not perform medical
 * diagnosis or make clinical decisions.</p>
 */
public class Patient {

    /** Unique identifier assigned by the database. */
    private long id;

    /** Patient's name or identifier entered by the user. */
    private String name;

    /** Patient's age in years. */
    private int age;

    /** Symptoms associated with this simulated patient. */
    private List<Symptom> symptoms = new ArrayList<>();

    /**
     * Creates an empty patient.
     *
     * <p>This constructor is useful when a framework or repository needs
     * to create an instance before assigning its values.</p>
     */
    public Patient() {
    }

    /**
     * Creates a patient without a database identifier.
     *
     * @param name patient's name
     * @param age patient's age in years
     */
    public Patient(String name, int age) {
        this.name = name;
        this.age = age;
    }

    /**
     * Creates a patient with an existing database identifier.
     *
     * @param id database identifier
     * @param name patient's name
     * @param age patient's age in years
     */
    public Patient(long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    /**
     * Returns the patient's database identifier.
     *
     * @return patient identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Assigns the patient's database identifier.
     *
     * @param id identifier to assign
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the patient's name.
     *
     * @return patient name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the patient's name.
     *
     * @param name new patient name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the patient's age.
     *
     * @return age in years
     */
    public int getAge() {
        return age;
    }

    /**
     * Updates the patient's age.
     *
     * @param age new age in years
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Returns the symptoms associated with the patient.
     *
     * @return mutable list of symptoms
     */
    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    /**
     * Replaces the patient's symptoms with a copy of the provided list.
     *
     * @param symptoms symptoms to assign; null is treated as an empty list
     */
    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms == null
                ? new ArrayList<>()
                : new ArrayList<>(symptoms);
    }

    /**
     * Adds one symptom to the patient.
     *
     * @param symptom symptom to add
     */
    public void addSymptom(Symptom symptom) {
        if (symptom != null) {
            symptoms.add(symptom);
        }
    }

    /**
     * Returns a readable summary for displaying the patient in a list.
     *
     * @return patient's name, age, and normalized symptoms
     */
    @Override
    public String toString() {
        String symptomNames = symptoms.stream()
                .map(Symptom::getNormalizedName)
                .distinct()
                .collect(Collectors.joining(", "));

        if (symptomNames.isBlank()) {
            symptomNames = "No symptoms";
        }

        return name + " (" + age + ") — " + symptomNames;
    }
}