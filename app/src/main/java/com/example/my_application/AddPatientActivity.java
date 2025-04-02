package com.example.my_application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class AddPatientActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Uri imageUri; // Store captured image URI
    private EditText editTextPatientName, editTextKnownDiagnosis, editTextCurrentDiagnosis;
    private Button buttonSave, captureUploadButton;
    private TextView textDate, textTime;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    private int PERMISSION_REQUEST_CODE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        requestCameraAccess();

        // Initialize UI elements
        editTextPatientName = findViewById(R.id.editTextPatientName);
        editTextKnownDiagnosis = findViewById(R.id.editTextKnownDiagnosis);
        editTextCurrentDiagnosis = findViewById(R.id.editTextCurrentDiagnosis);
        buttonSave = findViewById(R.id.buttonSave);
        captureUploadButton = findViewById(R.id.captureUploadButton);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);

        databaseHelper = new DatabaseHelper(this);

        // Set up RecyclerView for images
        imageAdapter = new ImageAdapter(selectedImages, this);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);

        // Display current date and time
        setCurrentDateTime();

        // Handle image capture/upload
        captureUploadButton.setOnClickListener(v -> showImageSelectionDialog());

        // Handle save button click
        buttonSave.setOnClickListener(v -> savePatientData());
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an option")
                .setItems(new String[]{"Capture Image", "Upload from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    public void requestCameraAccess() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
        else{
            openCamera();
        }
    }

    // ✅ Modified openCamera() to Save Image to Storage
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = createImageFile(); // Create a file to store the image
        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(this, "com.example.my_application.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (imageUri != null) {
                    selectedImages.add(imageUri); // Add captured image to the list
                    imageAdapter.notifyDataSetChanged(); // ✅ Update RecyclerView
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        selectedImages.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    selectedImages.add(data.getData());
                }
                imageAdapter.notifyDataSetChanged(); // ✅ Update RecyclerView
            }
        }
    }


    private void savePatientData() {
        String patientName = editTextPatientName.getText().toString().trim();
        String knownDiagnosis = editTextKnownDiagnosis.getText().toString().trim();
        String currentDiagnosis = editTextCurrentDiagnosis.getText().toString().trim();

        if (patientName.isEmpty() || knownDiagnosis.isEmpty() || currentDiagnosis.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.execSQL("INSERT INTO patients (name, visit_date, known_diagnosis, current_diagnosis) VALUES (?, date('now'), ?, ?)",
                    new String[]{patientName, knownDiagnosis, currentDiagnosis});
            db.close();
            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setCurrentDateTime() {
        // Get the current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Set values in TextViews
        textDate.setText(dateFormat.format(calendar.getTime())); // Display Date
        textTime.setText(timeFormat.format(calendar.getTime())); // Display Time
    }
}
