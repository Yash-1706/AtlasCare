// This file is now unused and all Firebase imports and usages have been removed.
// You may safely delete this file from your project.

package com.example.my_application;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHandler {

    public FirebaseHandler() {
    }
    
    public void getAllPatients(final FirebaseDataCallback callback) {
        callback.onSuccess(new ArrayList<>());
    }
    
    public void clearAllData(final FirebaseCallback callback) {
        callback.onSuccess();
    }
    
    public void addPatient(PatientModel patientModel, FirebaseCallback callback) {
        callback.onSuccess();
    }

    public void deletePatientById(String patientId, final FirebaseCallback callback) {
        callback.onSuccess();
    }

    public interface FirebaseCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    public interface FirebaseDataCallback {
        void onSuccess(List<PatientModel> patients);
        void onFailure(String error);
    }
}
