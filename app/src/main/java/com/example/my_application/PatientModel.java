package com.example.my_application;

public class PatientModel {
    private String name;
    private String caseNumber;

    public PatientModel(String name, String caseNumber) {
        this.name = name;
        this.caseNumber = caseNumber;
    }

    public String getName() {
        return name;
    }

    public String getCaseNumber() {
        return caseNumber;
    }
}
