package com.example.my_application;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class AtlasCareApplication extends Application {
    
    private static final String TAG = "AtlasCareApplication";
    private static final String DATABASE_URL = "https://atlascare-default-rtdb.asia-southeast1.firebasedatabase.app/";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase with detailed logging
        initializeFirebase();
    }
    
    private void initializeFirebase() {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                Log.d(TAG, "Initializing Firebase for the first time");
                FirebaseApp.initializeApp(this);
                
                // Enable disk persistence
                try {
                    // Use the specific database URL
                    FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
                    database.setPersistenceEnabled(true);
                    Log.d(TAG, "Firebase persistence enabled for database: " + DATABASE_URL);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to enable Firebase persistence: " + e.getMessage());
                }
                
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
                
                // Get the FirebaseApp instance
                FirebaseApp app = FirebaseApp.getInstance();
                if (app != null) {
                    FirebaseOptions options = app.getOptions();
                    Log.d(TAG, "Firebase config: ProjectID=" + options.getProjectId() + 
                          ", ApplicationID=" + options.getApplicationId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
