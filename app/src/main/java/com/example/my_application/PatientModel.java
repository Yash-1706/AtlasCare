package com.example.my_application;

import java.util.ArrayList;
import java.util.List;

public class PatientModel {
    private int id;
    private String name;
    private String knownDiagnosis;
    private String currentDiagnosis;
    private String date;
    private String time;
    private String firebaseKey;
    private List<String> imageUrls; // To store image references from Firebase Database
    private List<String> thumbnails; // To store direct thumbnail data

    public PatientModel() {
        // Initialize imageUrls to prevent null pointer exceptions
        this.imageUrls = new ArrayList<>();
        this.thumbnails = new ArrayList<>();
    }  // Required for Firebase

    public PatientModel(String name, String knownDiagnosis, String currentDiagnosis, String date, String time) {
        this.name = name;
        this.knownDiagnosis = knownDiagnosis;
        this.currentDiagnosis = currentDiagnosis;
        this.date = date;
        this.time = time;
        this.imageUrls = new ArrayList<>();
        this.thumbnails = new ArrayList<>();
    }

    // Getters & setters (required by Firebase)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKnownDiagnosis() { return knownDiagnosis; }
    public void setKnownDiagnosis(String knownDiagnosis) { this.knownDiagnosis = knownDiagnosis; }

    public String getCurrentDiagnosis() { return currentDiagnosis; }
    public void setCurrentDiagnosis(String currentDiagnosis) { this.currentDiagnosis = currentDiagnosis; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    
    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    // Helper method to add a single image URL
    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        this.imageUrls.add(imageUrl);
    }
    
    public List<String> getThumbnails() { return thumbnails; }
    public void setThumbnails(List<String> thumbnails) { this.thumbnails = thumbnails; }
    
    // Helper method to add a single thumbnail
    public void addThumbnail(String thumbnail) {
        if (this.thumbnails == null) {
            this.thumbnails = new ArrayList<>();
        }
        this.thumbnails.add(thumbnail);
    }
}
