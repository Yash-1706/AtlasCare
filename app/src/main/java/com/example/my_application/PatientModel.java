package com.example.my_application;

public class PatientModel {
    private String name;
    private String knownDiagnosis;
    private String currentDiagnosis;
    private String date;

    // Constructor (Ensure it accepts 3 parameters)
    public PatientModel(String name, String knownDiagnosis, String currentDiagnosis) {
        this.name = name;
        this.knownDiagnosis = knownDiagnosis;
        this.currentDiagnosis = currentDiagnosis;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getKnownDiagnosis() {
        return knownDiagnosis;
    }

    public String getCurrentDiagnosis() {
        return currentDiagnosis;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
