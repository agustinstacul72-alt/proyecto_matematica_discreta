package com.medset.medsetai.service;

import com.medset.medsetai.model.Patient;

import java.util.ArrayList;
import java.util.List;

/**
 * Shares the currently selected patients between application modules.
 *
 * <p>The selection is kept in memory and is not persisted. Patient records
 * themselves remain stored in SQLite. The analysis module can retrieve this
 * selection and use each patient's normalized symptoms as mathematical sets.</p>
 */
public final class PatientSelectionContext {

    /** Current selection shared with the analysis module. */
    private static List<Patient> selectedPatients = new ArrayList<>();

    /**
     * Prevents instances of this utility class.
     */
    private PatientSelectionContext() {
    }

    /**
     * Replaces the currently selected patients.
     *
     * @param patients patients selected for analysis; null clears the selection
     */
    public static void setSelectedPatients(List<Patient> patients) {
        selectedPatients = patients == null
                ? new ArrayList<>()
                : new ArrayList<>(patients);
    }

    /**
     * Returns a read-only snapshot of the current selection.
     *
     * @return selected patients
     */
    public static List<Patient> getSelectedPatients() {
        return List.copyOf(selectedPatients);
    }

    /**
     * Removes all patients from the current selection.
     */
    public static void clear() {
        selectedPatients.clear();
    }
}