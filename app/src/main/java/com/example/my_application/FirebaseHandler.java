package com.example.my_application;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseHandler {

    private static final String TAG = "FirebaseHandler";
    private final DatabaseReference databaseRef;
    private static final int TIMEOUT_SECONDS = 60; // Increased timeout for image uploads
    
    // The specific URL for your Firebase database
    private static final String DATABASE_URL = "https://atlascare-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public FirebaseHandler() {
        // Initialize Firebase database reference
        FirebaseDatabase database;
        
        try {
            // Use the specific database URL
            database = FirebaseDatabase.getInstance(DATABASE_URL);
            Log.d(TAG, "Got Firebase database instance with URL: " + DATABASE_URL);
            
            databaseRef = database.getReference("patients");
            Log.d(TAG, "Database reference path: " + databaseRef.toString());
            
            // Test database connectivity
            testDatabaseConnectivity();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to make initialization failure obvious
        }
    }
    
    /**
     * Retrieve all patients from Firebase
     */
    public void getAllPatients(final FirebaseDataCallback callback) {
        try {
            Log.d(TAG, "Retrieving all patients from Firebase");
            
            // Set a timeout handler
            final Handler timeoutHandler = new Handler();
            final Runnable timeoutRunnable = () -> {
                Log.e(TAG, "Firebase read operation timed out after " + TIMEOUT_SECONDS + " seconds");
                callback.onFailure("Read operation timed out. Check your internet connection.");
            };
            
            // Schedule the timeout
            timeoutHandler.postDelayed(timeoutRunnable, TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            
            // Force a refresh of the data
            databaseRef.keepSynced(true);
            
            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Cancel the timeout since we got data
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    Log.d(TAG, "Firebase data snapshot exists: " + dataSnapshot.exists());
                    Log.d(TAG, "Firebase data snapshot has children: " + dataSnapshot.hasChildren());
                    
                    List<PatientModel> patients = new ArrayList<>();
                    
                    if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                        Log.d(TAG, "No patients found in Firebase");
                        callback.onSuccess(patients); // Return empty list
                        return;
                    }
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            PatientModel patient = snapshot.getValue(PatientModel.class);
                            if (patient != null) {
                                patients.add(patient);
                                Log.d(TAG, "Retrieved patient: " + patient.getName());
                                
                                // Log image URLs and thumbnails
                                if (patient.getImageUrls() != null) {
                                    Log.d(TAG, "Patient has " + patient.getImageUrls().size() + " image URLs");
                                } else {
                                    Log.d(TAG, "Patient has no image URLs");
                                }
                                
                                if (patient.getThumbnails() != null) {
                                    Log.d(TAG, "Patient has " + patient.getThumbnails().size() + " thumbnails");
                                    // Check if thumbnails field exists in the database
                                    if (snapshot.hasChild("thumbnails")) {
                                        Log.d(TAG, "'thumbnails' field exists in database");
                                    } else {
                                        Log.d(TAG, "'thumbnails' field DOES NOT exist in database");
                                    }
                                } else {
                                    Log.d(TAG, "Patient has no thumbnails");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing patient data: " + e.getMessage());
                        }
                    }
                    
                    Log.d(TAG, "Successfully retrieved " + patients.size() + " patients from Firebase");
                    callback.onSuccess(patients);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Cancel the timeout since we got an error
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    
                    Log.e(TAG, "Firebase read cancelled: " + databaseError.getMessage());
                    callback.onFailure("Error reading from database: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception retrieving patients from Firebase: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Clears all data from Firebase
     */
    public void clearAllData(final FirebaseCallback callback) {
        try {
            Log.d(TAG, "Attempting to clear all data from Firebase");
            
            // Set a timeout handler
            final Handler timeoutHandler = new Handler();
            final Runnable timeoutRunnable = () -> {
                Log.e(TAG, "Firebase clear operation timed out after " + TIMEOUT_SECONDS + " seconds");
                callback.onFailure("Operation timed out. Check your internet connection.");
            };
            
            // Schedule the timeout
            timeoutHandler.postDelayed(timeoutRunnable, TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            
            // Remove all data at the patients reference
            databaseRef.removeValue()
                    .addOnSuccessListener(unused -> {
                        // Now clear all images from database
                        clearAllImages(callback, timeoutHandler, timeoutRunnable);
                    })
                    .addOnFailureListener(e -> {
                        // Cancel the timeout if operation fails with an error
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        Log.e(TAG, "Failed to clear data from Firebase: " + e.getMessage());
                        callback.onFailure("Error: " + e.getMessage());
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Exception clearing data from Firebase: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Clears all images from Firebase Database
     */
    private void clearAllImages(FirebaseCallback callback, Handler timeoutHandler, Runnable timeoutRunnable) {
        try {
            Log.d(TAG, "Attempting to clear all images from Firebase Database");
            
            // Clear the images node in the database
            FirebaseDatabase.getInstance().getReference().child("patient_images").removeValue()
                .addOnSuccessListener(unused -> {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    Log.d(TAG, "Successfully cleared all data from Firebase");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    Log.e(TAG, "Failed to clear images: " + e.getMessage());
                    callback.onFailure("Failed to clear images: " + e.getMessage());
                });
            
        } catch (Exception e) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            Log.e(TAG, "Exception clearing images from Firebase: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Exception: " + e.getMessage());
        }
    }
    
    private void testDatabaseConnectivity() {
        try {
            Log.d(TAG, "Testing Firebase database connectivity...");
            
            // Check if database is connected
            databaseRef.getDatabase().getReference(".info/connected").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean connected = snapshot.getValue(Boolean.class);
                        if (connected != null && connected) {
                            Log.d(TAG, "Firebase is connected");
                        } else {
                            Log.e(TAG, "Firebase is not connected");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase connectivity check failed: " + error.getMessage());
                    }
                });
                
            // Check database rules by trying to read something
            databaseRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Firebase read check success. Has children: " + snapshot.hasChildren());
                    if (snapshot.hasChildren()) {
                        Log.d(TAG, "Existing data found in Firebase database");
                    } else {
                        Log.d(TAG, "No existing data in Firebase database");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase read check failed: " + error.getMessage() + 
                          ", code: " + error.getCode());
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Exception during Firebase connectivity test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addPatient(PatientModel patientModel, FirebaseCallback callback) {
        try {
            Log.d(TAG, "Attempting to add patient to Firebase: " + patientModel.getName());
            Log.d(TAG, "Patient has " + (patientModel.getImageUrls() != null ? patientModel.getImageUrls().size() : 0) + " image URLs");
            Log.d(TAG, "Patient has " + (patientModel.getThumbnails() != null ? patientModel.getThumbnails().size() : 0) + " thumbnails");
            
            String patientId = databaseRef.push().getKey();

            // Set a timeout handler
            final Handler timeoutHandler = new Handler();
            final Runnable timeoutRunnable = () -> {
                Log.e(TAG, "Firebase operation timed out after " + TIMEOUT_SECONDS + " seconds");
                callback.onFailure("Operation timed out. Check your internet connection.");
            };
            
            // Schedule the timeout
            timeoutHandler.postDelayed(timeoutRunnable, TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));

            if (patientId != null) {
                Log.d(TAG, "Generated patient ID: " + patientId);
                databaseRef.child(patientId).setValue(patientModel)
                        .addOnSuccessListener(unused -> {
                            // Cancel the timeout if operation succeeds
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.d(TAG, "Successfully added patient to Firebase");
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            // Cancel the timeout if operation fails with an error
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            Log.e(TAG, "Failed to add patient to Firebase: " + e.getMessage());
                            callback.onFailure("Error: " + e.getMessage());
                        });
            } else {
                // Cancel the timeout if we can't even get a patient ID
                timeoutHandler.removeCallbacks(timeoutRunnable);
                Log.e(TAG, "Error: Could not generate patient ID");
                callback.onFailure("Error creating patient ID");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception adding patient to Firebase: " + e.getMessage());
            e.printStackTrace();
            callback.onFailure("Exception: " + e.getMessage());
        }
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
