package com.example.my_application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextPatientName, editTextKnownDiagnosis, editTextCurrentDiagnosis;
    private Button buttonSave, captureUploadButton;
    private TextView textDate, textTime;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<Uri> imageUris = new ArrayList<>();
    private DatabaseHelper databaseHelper;

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

        if (captureUploadButton == null) {
            Log.e("AddPatientActivity", "captureUploadButton is NULL!");
        }

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
        imageAdapter = new ImageAdapter(imageUris, this);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imageAdapter);

        // Display current date and time
        setCurrentDateTime();

        // Handle image capture/upload
        captureUploadButton.setOnClickListener(v -> openImagePicker());

        // Handle save button click
        buttonSave.setOnClickListener(v -> savePatientData());
    }

    private void openImagePicker() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selection
        startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) { // Single image selected
                imageUris.add(data.getData());
            }
            imageAdapter.notifyDataSetChanged();
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
