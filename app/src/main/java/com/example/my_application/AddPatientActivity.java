package com.example.my_application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AddPatientActivity extends AppCompatActivity {

    private static final String TAG = "AddPatientActivity";
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int MAX_IMAGE_SIZE = 500; // Maximum image dimension

    private Uri imageUri; // Store captured image URI
    private EditText editTextPatientName, editTextKnownDiagnosis, editTextCurrentDiagnosis;
    private Button buttonSave, captureUploadButton;
    private TextView textDate, textTime;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private FirebaseHandler firebaseHandler;
    private DatabaseReference databaseRef;

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

        // Initialize Firebase
        firebaseHandler = new FirebaseHandler();
        databaseRef = FirebaseDatabase.getInstance().getReference();

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
            requestPermissions(new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }
        else{
            openCamera();
        }
    }

    // Modified openCamera() to Save Image to Storage
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
                    imageAdapter.notifyDataSetChanged(); // Update RecyclerView
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri selectedUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(selectedUri);
                    }
                } else if (data.getData() != null) {
                    Uri selectedUri = data.getData();
                    selectedImages.add(selectedUri);
                }
                imageAdapter.notifyDataSetChanged(); // Update RecyclerView
            }
            
            // Show number of selected images
            if (!selectedImages.isEmpty()) {
                Toast.makeText(this, selectedImages.size() + " images selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePatientData() {
        String name = editTextPatientName.getText().toString().trim();
        String knownDiagnosis = editTextKnownDiagnosis.getText().toString().trim();
        String currentDiagnosis = editTextCurrentDiagnosis.getText().toString().trim();
        String date = textDate.getText().toString().trim();
        String time = textTime.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || knownDiagnosis.isEmpty() || currentDiagnosis.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PatientModel object
        PatientModel patient = new PatientModel(name, knownDiagnosis, currentDiagnosis, date, time);

        // Show progress indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images and saving patient data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // If there are no images, just save the patient data
        if (selectedImages.isEmpty()) {
            savePatientWithoutImages(patient, progressDialog);
            return;
        }
        
        // Check if we have too many images
        if (selectedImages.size() > 5) {
            Toast.makeText(this, "Processing up to 5 images for better performance", Toast.LENGTH_SHORT).show();
            List<Uri> limitedImages = new ArrayList<>(selectedImages.subList(0, 5));
            selectedImages.clear();
            selectedImages.addAll(limitedImages);
        }

        // Upload images directly to Firebase Database
        uploadImagesToDatabase(patient, progressDialog);
    }

    private void uploadImagesToDatabase(PatientModel patient, ProgressDialog progressDialog) {
        final List<String> imageUrls = new ArrayList<>();
        final List<String> thumbnails = new ArrayList<>();
        final AtomicInteger uploadCount = new AtomicInteger(0);
        final int totalImages = selectedImages.size();
        
        // If there are no images, save the patient without images
        if (selectedImages.isEmpty()) {
            Log.d(TAG, "No images to upload, saving patient without images");
            savePatientToFirebase(patient, progressDialog);
            return;
        }

        // Create a unique ID for this patient
        String patientId = UUID.randomUUID().toString();
        Log.d(TAG, "Generated patient ID: " + patientId + " for patient: " + patient.getName());
        
        // Create a reference to store image data
        DatabaseReference imagesRef = databaseRef.child("patient_images").child(patientId);
        
        // Process all images first to extract thumbnails
        for (int i = 0; i < selectedImages.size(); i++) {
            try {
                Uri imageUri = selectedImages.get(i);
                // Convert the image to a bitmap with reduced resolution
                Bitmap bitmap = getResizedBitmap(imageUri, MAX_IMAGE_SIZE);
                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode image at index " + i);
                    continue;
                }
                
                // Create a smaller thumbnail for direct embedding in patient data
                Bitmap thumbnail = createThumbnail(bitmap, 150); // 150px thumbnail
                String thumbnailBase64 = bitmapToBase64(thumbnail, 70); // Higher quality for thumbnails
                
                // Add the thumbnail to our list
                thumbnails.add(thumbnailBase64);
                
                // Create a placeholder URL (we'll update this later if the full image uploads)
                imageUrls.add("placeholder_" + i);
                
                Log.d(TAG, "Processed thumbnail for image " + i);
            } catch (IOException e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
            }
        }
        
        // Save the patient with thumbnails immediately
        if (!thumbnails.isEmpty()) {
            patient.setThumbnails(thumbnails);
            patient.setImageUrls(imageUrls); // Placeholder URLs for now
            
            // Save patient data first
            savePatientToFirebase(patient, progressDialog);
            
            // Then try to upload the full images in the background
            uploadFullImagesInBackground(patientId);
        } else {
            // No thumbnails could be processed
            savePatientToFirebase(patient, progressDialog);
        }
    }
    
    private void uploadFullImagesInBackground(String patientId) {
        // Create a reference to store image data
        DatabaseReference imagesRef = databaseRef.child("patient_images").child(patientId);
        
        for (int i = 0; i < selectedImages.size(); i++) {
            try {
                Uri imageUri = selectedImages.get(i);
                final int imageIndex = i;
                
                // Convert the image to a bitmap with reduced resolution
                Bitmap bitmap = getResizedBitmap(imageUri, MAX_IMAGE_SIZE);
                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode image at index " + i + " for background upload");
                    continue;
                }
                
                // Convert full bitmap to Base64 string with higher compression
                String base64Image = bitmapToBase64(bitmap, 50); // Use 50% quality for better compression
                
                // Create a unique image ID
                String imageId = "image_" + imageIndex;
                Log.d(TAG, "Uploading full image " + imageId + " in background for patient " + patientId);
                
                // Save the full image data to Firebase Database
                imagesRef.child(imageId).setValue(base64Image)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Background image upload successful for image " + imageIndex);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Background image upload failed: " + e.getMessage());
                    });
                
            } catch (IOException e) {
                Log.e(TAG, "Error processing image for background upload: " + e.getMessage());
            }
        }
    }

    private Bitmap getResizedBitmap(Uri imageUri, int maxSize) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = getContentResolver().openInputStream(imageUri);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();
        
        return resizeBitmap(bitmap, maxSize);
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        if (bitmap == null) return null;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap; // No need to resize
        }
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    private String bitmapToBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void savePatientToFirebase(PatientModel patient, ProgressDialog progressDialog) {
        // Log the patient data before saving
        Log.d(TAG, "Saving patient: " + patient.getName());
        Log.d(TAG, "Image URLs: " + (patient.getImageUrls() != null ? patient.getImageUrls().size() : 0));
        Log.d(TAG, "Thumbnails: " + (patient.getThumbnails() != null ? patient.getThumbnails().size() : 0));
        
        if (patient.getThumbnails() != null && !patient.getThumbnails().isEmpty()) {
            Log.d(TAG, "First thumbnail length: " + patient.getThumbnails().get(0).length());
        }
        
        firebaseHandler.addPatient(patient, new FirebaseHandler.FirebaseCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(AddPatientActivity.this, 
                        "Patient saved with " + patient.getImageUrls().size() + " images", 
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(AddPatientActivity.this, 
                        "Failed to save patient data: " + error, 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePatientWithoutImages(PatientModel patient, ProgressDialog existingDialog) {
        ProgressDialog progressDialog = existingDialog;
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Saving patient data without images...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        final ProgressDialog finalDialog = progressDialog;

        firebaseHandler.addPatient(patient, new FirebaseHandler.FirebaseCallback() {
            @Override
            public void onSuccess() {
                finalDialog.dismiss();
                Toast.makeText(AddPatientActivity.this, 
                        "Patient data saved successfully", 
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                finalDialog.dismiss();
                Toast.makeText(AddPatientActivity.this, 
                        "Failed to save patient data: " + error, 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCurrentDateTime() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Display date and time
        textDate.setText(dateFormat.format(calendar.getTime()));
        textTime.setText(timeFormat.format(calendar.getTime()));
    }

    private Bitmap createThumbnail(Bitmap original, int size) {
        // Calculate dimensions while maintaining aspect ratio
        int width = original.getWidth();
        int height = original.getHeight();
        
        float ratio = Math.min((float) size / width, (float) size / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}
