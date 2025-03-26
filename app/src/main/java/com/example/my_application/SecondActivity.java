package com.example.my_application;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {
    private CheckBox checkBoxExistingPatient;
    private EditText etSearchPatient, etPatientName, etDate, etTime, etDiagnosis;
    private Button btnSave;
    private ArrayList<String> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Initialize Views
        checkBoxExistingPatient = findViewById(R.id.checkBoxExistingPatient);
        etSearchPatient = findViewById(R.id.etSearchPatient);
        etPatientName = findViewById(R.id.etPatientName);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDiagnosis = findViewById(R.id.etDiagnosis);  // Initialize diagnosis field
        btnSave = findViewById(R.id.btnSave);

        // Sample patient list (Replace with real data)
        patientList = new ArrayList<>();
        patientList.add("John Doe");
        patientList.add("Jane Smith");
        patientList.add("Alice Johnson");

        // Set System Date & Time
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        etDate.setText(currentDate);
        etTime.setText(currentTime);

        // Checkbox Logic: Enable/Disable Fields
        checkBoxExistingPatient.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etSearchPatient.setEnabled(true); // Enable Search Field
                etPatientName.setEnabled(false);  // Disable Patient Name Input
                etPatientName.setText("");        // Clear Patient Name
            } else {
                etSearchPatient.setEnabled(false); // Disable Search Field
                etSearchPatient.setText("");       // Clear Search Field
                etPatientName.setEnabled(true);    // Enable Patient Name Input
            }
        });

        // Search Functionality: Filter Patient List
        etSearchPatient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    for (String patient : patientList) {
                        if (patient.equalsIgnoreCase(query)) {
                            Toast.makeText(SecondActivity.this, "Patient Found: " + patient, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save Button Click Event
        btnSave.setOnClickListener(v -> {
            String patientName = etPatientName.getText().toString().trim();
            String searchPatient = etSearchPatient.getText().toString().trim();
            String date = etDate.getText().toString();
            String time = etTime.getText().toString();
            String diagnosis = etDiagnosis.getText().toString().trim();  // Get diagnosis text

            // Validation: Check if all fields are filled
            if ((!checkBoxExistingPatient.isChecked() && patientName.isEmpty()) ||
                    (checkBoxExistingPatient.isChecked() && searchPatient.isEmpty()) ||
                    date.isEmpty() || time.isEmpty() || diagnosis.isEmpty()) {
                Toast.makeText(SecondActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SecondActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();

                // Redirect to Main Screen
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            String patientName = etPatientName.getText().toString().trim();
            String diagnosis = etDiagnosis.getText().toString().trim();

            // Validation Check
            if (patientName.isEmpty() || diagnosis.isEmpty()) {
                Toast.makeText(SecondActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // If everything is filled correctly
            Toast.makeText(SecondActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();

            // Redirect to Main Screen & Clear Activity Stack
            Intent intent = new Intent(SecondActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }
}
