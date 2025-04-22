package com.example.my_application;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailActivity extends AppCompatActivity {

    private static final String TAG = "PatientDetailActivity";
    private TextView tvName, tvKnownDiagnosis, tvCurrentDiagnosis, tvDate, tvTime;
    private RecyclerView rvImages;
    private PatientImageAdapter imageAdapter;
    private String patientName, knownDiagnosis, currentDiagnosis, date, time;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<String> thumbnails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Patient Details");
        }

        // Initialize views
        tvName = findViewById(R.id.tvPatientName);
        tvKnownDiagnosis = findViewById(R.id.tvKnownDiagnosis);
        tvCurrentDiagnosis = findViewById(R.id.tvCurrentDiagnosis);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        rvImages = findViewById(R.id.rvImages);

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            patientName = intent.getStringExtra("patientName");
            knownDiagnosis = intent.getStringExtra("knownDiagnosis");
            currentDiagnosis = intent.getStringExtra("currentDiagnosis");
            date = intent.getStringExtra("date");
            time = intent.getStringExtra("time");
            
            // Get image URLs and thumbnails
            imageUrls = intent.getStringArrayListExtra("imageUrls");
            thumbnails = intent.getStringArrayListExtra("thumbnails");
            
            // Log the received data
            Log.d(TAG, "Received patient: " + patientName);
            Log.d(TAG, "Image URLs: " + (imageUrls != null ? imageUrls.size() : 0));
            Log.d(TAG, "Thumbnails: " + (thumbnails != null ? thumbnails.size() : 0));
            
            // Check if the extras exist in the intent
            Log.d(TAG, "Intent has imageUrls extra: " + intent.hasExtra("imageUrls"));
            Log.d(TAG, "Intent has thumbnails extra: " + intent.hasExtra("thumbnails"));

            // Set data to views
            tvName.setText(patientName);
            tvKnownDiagnosis.setText("Known Diagnosis: " + knownDiagnosis);
            tvCurrentDiagnosis.setText("Current Diagnosis: " + currentDiagnosis);
            tvDate.setText("Date: " + date);
            tvTime.setText("Time: " + time);

            // Setup images recycler view
            setupImagesRecyclerView();
        }
    }

    private void setupImagesRecyclerView() {
        // Check if we have thumbnails first
        if (thumbnails != null && !thumbnails.isEmpty()) {
            Log.d(TAG, "Using " + thumbnails.size() + " thumbnails for patient: " + patientName);
            findViewById(R.id.tvNoImages).setVisibility(View.GONE);
            rvImages.setVisibility(View.VISIBLE);
            
            // Create a list of image URLs with data:image/jpeg;base64 format
            List<String> displayImages = new ArrayList<>();
            for (String thumbnail : thumbnails) {
                // Make sure the thumbnail has the proper prefix
                if (!thumbnail.startsWith("data:image/")) {
                    displayImages.add("data:image/jpeg;base64," + thumbnail);
                } else {
                    displayImages.add(thumbnail);
                }
            }
            
            // Log the first thumbnail for debugging
            if (!displayImages.isEmpty()) {
                String firstImage = displayImages.get(0);
                Log.d(TAG, "First thumbnail starts with: " + firstImage.substring(0, Math.min(30, firstImage.length())));
            }
            
            imageAdapter = new PatientImageAdapter(displayImages, this);
            rvImages.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns grid
            rvImages.setAdapter(imageAdapter);
            return;
        }
        
        // Fall back to image URLs if no thumbnails
        if (imageUrls != null && !imageUrls.isEmpty()) {
            findViewById(R.id.tvNoImages).setVisibility(View.GONE);
            rvImages.setVisibility(View.VISIBLE);
            Log.d(TAG, "Found " + imageUrls.size() + " image URLs for patient: " + patientName);
            for (int i = 0; i < imageUrls.size(); i++) {
                Log.d(TAG, "Image " + i + ": " + imageUrls.get(i));
            }

            imageAdapter = new PatientImageAdapter(imageUrls, this);
            rvImages.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns grid
            rvImages.setAdapter(imageAdapter);
            return;
        }
        
        // No images available
        findViewById(R.id.tvNoImages).setVisibility(View.VISIBLE);
        rvImages.setVisibility(View.GONE);
        Log.d(TAG, "No images found for patient: " + patientName);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
