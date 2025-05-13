package com.example.my_application;

import android.app.Application;
import android.util.Log;

public class AtlasCareApplication extends Application {
    
    private static final String TAG = "AtlasCareApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase with detailed logging
        initializeFirebase();
    }
    
    private void initializeFirebase() {
        try {
            Log.d(TAG, "Firebase initialization removed (no longer using Firebase)");
            // All Firebase initialization code removed.
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
