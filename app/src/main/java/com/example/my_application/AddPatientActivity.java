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

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    private int PERMISSION_REQUEST_CODE = 1;

    private PatientModel pendingPatient = null;
    private ProgressDialog pendingProgressDialog = null;

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
        buttonSave.setEnabled(true); // Ensure enabled at start
        captureUploadButton = findViewById(R.id.captureUploadButton);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);

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

        // Check if patient already exists (by name)
        String patientName = getIntent().getStringExtra("patient_name");
        if (patientName != null && !patientName.isEmpty()) {
            // Find patient by name
            PatientEntity patientEntity = null;
            for (PatientEntity entity : AppDatabase.getInstance(this).patientDao().getAllPatientsSorted()) {
                if (entity.name.equalsIgnoreCase(patientName)) {
                    patientEntity = entity;
                    break;
                }
            }
            if (patientEntity != null) {
                int visitCount = AppDatabase.getInstance(this).visitDao().getVisitCountForPatient(patientEntity.id);
                if (visitCount >= 1) {
                    // Hide known diagnosis for 2nd+ visit
                    editTextKnownDiagnosis.setVisibility(View.GONE);
                    TextView knownDiagnosisLabel = findViewById(R.id.labelKnownDiagnosis);
                    if (knownDiagnosisLabel != null) knownDiagnosisLabel.setVisibility(View.GONE);
                }
            }
        }
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
        buttonSave.setEnabled(false);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Save patient data
        savePatientWithoutImages(patient, progressDialog);
    }

    private void savePatientWithoutImages(PatientModel patient, ProgressDialog progressDialog) {
        // Convert selected images to Base64 and add to patient
        List<String> fullImages = new ArrayList<>();
        for (Uri uri : selectedImages) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap fullBitmap = BitmapFactory.decodeStream(inputStream);
                if (fullBitmap != null) {
                    String fullBase64 = bitmapToBase64(fullBitmap, 90);
                    if (!fullBase64.startsWith("data:image/")) {
                        fullBase64 = "data:image/jpeg;base64," + fullBase64;
                    }
                    fullImages.add(fullBase64);
                }
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                progressDialog.dismiss();
                Toast.makeText(this, "Error saving patient data", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Save patient to Room DB
        PatientEntity entity = new PatientEntity();
        entity.name = patient.getName();
        entity.knownDiagnosis = patient.getKnownDiagnosis();
        entity.currentDiagnosis = patient.getCurrentDiagnosis();
        entity.date = patient.getDate();
        entity.time = patient.getTime();
        entity.imageUrls = fullImages;
        AppDatabase db = AppDatabase.getInstance(this);
        // Insert patient if new (first visit)
        PatientEntity existing = null;
        for (PatientEntity e : db.patientDao().getAllPatientsSorted()) {
            if (e.name.equalsIgnoreCase(entity.name)) {
                existing = e;
                break;
            }
        }
        int patientId = -1;
        if (existing == null) {
            db.patientDao().insertPatient(entity);
            // Get patientId of inserted patient
            List<PatientEntity> all = db.patientDao().getAllPatientsSorted();
            for (PatientEntity e : all) {
                if (e.name.equalsIgnoreCase(entity.name)) {
                    patientId = e.id;
                    break;
                }
            }
        } else {
            patientId = existing.id;
        }
        // Add visit record
        VisitEntity visit = new VisitEntity();
        visit.patientId = patientId;
        visit.date = patient.getDate();
        visit.notes = patient.getCurrentDiagnosis(); // Store current diagnosis in visit notes

        // Convert selected images to Base64 and add to visit
        List<String> visitImages = new ArrayList<>();
        for (Uri uri : selectedImages) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap fullBitmap = BitmapFactory.decodeStream(inputStream);
                if (fullBitmap != null) {
                    String fullBase64 = bitmapToBase64(fullBitmap, 90);
                    if (!fullBase64.startsWith("data:image/")) {
                        fullBase64 = "data:image/jpeg;base64," + fullBase64;
                    }
                    visitImages.add(fullBase64);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        visit.imageUrls = visitImages;

        db.visitDao().insertVisit(visit);
        progressDialog.dismiss();
        finish();
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
