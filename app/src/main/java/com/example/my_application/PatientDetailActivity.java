package com.example.my_application;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailActivity extends AppCompatActivity {

    private static final String TAG = "PatientDetailActivity";
    private TextView tvName, tvKnownDiagnosis, tvCurrentDiagnosis, tvDate, tvTime;
    private RecyclerView rvImages, rvVisits;
    private PatientImageAdapter imageAdapter;
    private VisitAdapter visitAdapter;
    private String patientName, knownDiagnosis, currentDiagnosis, date, time;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<String> thumbnails = new ArrayList<>();
    private ArrayList<VisitModel> visits = new ArrayList<>();
    private Button btnAddVisit;

    private static final int PICK_IMAGES_REQUEST_CODE = 1010;
    private ArrayList<Uri> pendingImageUris = new ArrayList<>();
    private GoogleSignInAccount googleSignInAccount;
    private GoogleDriveUtil driveUtil;
    private PatientImageAdapter tempImageAdapter;
    private ArrayList<String> tempImageDriveLinks = new ArrayList<>();

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
        rvVisits = findViewById(R.id.rvVisits);
        btnAddVisit = findViewById(R.id.btnAddVisit);

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

            // Setup visits recycler view
            rvVisits.setLayoutManager(new LinearLayoutManager(this));
            visitAdapter = new VisitAdapter(visits);
            rvVisits.setAdapter(visitAdapter);

            // Add visit button click listener
            btnAddVisit.setOnClickListener(v -> showAddVisitDialog());

            // (Assume you already have GoogleSignInAccount from sign-in)
            googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleSignInAccount != null) {
                driveUtil = new GoogleDriveUtil(this, googleSignInAccount);
            }
        }
    }

    private void setupImagesRecyclerView() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            findViewById(R.id.tvNoImages).setVisibility(View.GONE);
            rvImages.setVisibility(View.VISIBLE);
            imageAdapter = new PatientImageAdapter(imageUrls, imageUrls, this);
            rvImages.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns grid
            rvImages.setAdapter(imageAdapter);
            return;
        }
        findViewById(R.id.tvNoImages).setVisibility(View.VISIBLE);
        rvImages.setVisibility(View.GONE);
    }

    private void showAddVisitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_visit, null);
        builder.setView(dialogView);

        EditText etCurrentDiagnosis = dialogView.findViewById(R.id.etCurrentDiagnosis);
        TextView tvVisitDate = dialogView.findViewById(R.id.tvVisitDate);
        TextView tvVisitTime = dialogView.findViewById(R.id.tvVisitTime);
        Button btnAddPrescriptionImages = dialogView.findViewById(R.id.btnAddPrescriptionImages);
        RecyclerView rvPrescriptionImages = dialogView.findViewById(R.id.rvPrescriptionImages);

        // Set current date and time
        java.util.Date now = new java.util.Date();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(now);
        String time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(now);
        tvVisitDate.setText(date);
        tvVisitTime.setText(time);

        // TODO: Add logic for prescription images (use existing image picker logic if available)
        // For now, just setup empty image adapter
        ArrayList<String> imageUrls = new ArrayList<>();
        tempImageAdapter = new PatientImageAdapter(imageUrls, imageUrls, this);
        rvPrescriptionImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPrescriptionImages.setAdapter(tempImageAdapter);
        btnAddPrescriptionImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGES_REQUEST_CODE);
        });

        builder.setTitle("Add Visit");
        builder.setPositiveButton("Save", (dialog, which) -> {
            String diagnosis = etCurrentDiagnosis.getText().toString().trim();
            if (TextUtils.isEmpty(diagnosis)) {
                Toast.makeText(this, "Diagnosis cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            VisitModel visit = new VisitModel();
            visit.setDiagnosis(diagnosis);
            visit.setDate(date);
            visit.setTime(time);
            visit.setImageUrls(new ArrayList<>(tempImageDriveLinks));
            visits.add(visit);
            visitAdapter.notifyItemInserted(visits.size() - 1);
            // TODO: Save visit to Firebase under this patient
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK) {
            pendingImageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    pendingImageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                pendingImageUris.add(data.getData());
            }
            // Upload all selected images to Google Drive
            uploadSelectedImagesToDrive();
        }
    }

    private void uploadSelectedImagesToDrive() {
        if (driveUtil == null) {
            Toast.makeText(this, "Google Drive not initialized!", Toast.LENGTH_SHORT).show();
            return;
        }
        tempImageDriveLinks.clear();
        for (Uri uri : pendingImageUris) {
            java.io.File file = FileUtil.getFileFromUri(this, uri);
            if (file != null) {
                driveUtil.uploadFile(file, "image/jpeg", new GoogleDriveUtil.DriveUploadCallback() {
                    @Override
                    public void onSuccess(String fileId, String webViewLink) {
                        tempImageDriveLinks.add(webViewLink);
                        tempImageAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PatientDetailActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
