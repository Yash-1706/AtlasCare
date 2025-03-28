package com.example.my_application;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

public class SecondActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 200;
    private static final int PERMISSION_REQUEST_CODE = 300;

    private EditText etPatientName, etDate, etTime, etKnownDiagnosis, etCurrentDiagnosis;
    private Button btnSave, btnCaptureImage;
    private ImageView ivPatientImage;
    private Uri cameraImageUri;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Initialize Views
        etPatientName = findViewById(R.id.etPatientName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etKnownDiagnosis = findViewById(R.id.etKnownDiagnosis);
        etCurrentDiagnosis = findViewById(R.id.etCurrentDiagnosis);
        btnSave = findViewById(R.id.btnSave);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPatientImage = findViewById(R.id.ivPatientImage);

        // Set System Date & Time
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        etDate.setText(currentDate);
        etTime.setText(currentTime);

        // Capture Image Button Click
        btnCaptureImage.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                showImagePickerDialog();
            }
        });

        // Save Button Click Event
        btnSave.setOnClickListener(v -> savePatientData());
    }

    // Show dialog to choose Camera or Gallery
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image")
                .setItems(new CharSequence[]{"Capture Image", "Upload from Gallery"},
                        (dialog, which) -> {
                            if (which == 0) {
                                openCamera();
                            } else {
                                openGallery();
                            }
                        })
                .show();
    }

    // Open Camera
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } catch (IOException e) {
            Log.e("DEBUG_CAMERA", "Error creating image file: " + e.getMessage());
            Toast.makeText(this, "Error while creating file!", Toast.LENGTH_SHORT).show();
        }
    }

    // Open Gallery
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST);
    }

    // Create a file for the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = new File(storageDir, imageFileName + ".jpg");
        currentPhotoPath = image.getAbsolutePath();
        Log.d("DEBUG_CAMERA", "Image file created: " + currentPhotoPath);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                if (cameraImageUri != null) {
                    ivPatientImage.setImageURI(cameraImageUri);
                    ivPatientImage.setVisibility(View.VISIBLE);
                } else {
                    File imgFile = new File(currentPhotoPath);
                    if (imgFile.exists()) {
                        cameraImageUri = Uri.fromFile(imgFile);
                        ivPatientImage.setImageURI(cameraImageUri);
                        ivPatientImage.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Error displaying image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == GALLERY_REQUEST && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    ivPatientImage.setImageURI(selectedImageUri);
                    ivPatientImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // Check and request permissions
    private boolean checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Save patient data after validation
    private void savePatientData() {
        String patientName = etPatientName.getText().toString().trim();
        String knownDiagnosis = etKnownDiagnosis.getText().toString().trim();
        String currentDiagnosis = etCurrentDiagnosis.getText().toString().trim();

        if (patientName.isEmpty() || knownDiagnosis.isEmpty() || currentDiagnosis.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
