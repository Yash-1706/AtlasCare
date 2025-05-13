package com.example.my_application;

import java.util.ArrayList;
import java.util.List;

// NOTE: This repository is now obsolete as data is stored on Google Drive. You may keep it for temporary caching or remove it.
public class PatientRepository {
    private static final List<PatientModel> patientList = new ArrayList<>();

    public static List<PatientModel> getAllPatients() {
        return new ArrayList<>(patientList);
    }

    public static void addPatient(PatientModel patient) {
        patientList.add(patient);
    }

    public static void clearAll() {
        patientList.clear();
    }
}
