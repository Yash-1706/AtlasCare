package com.example.my_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {

    private EditText editTextPatientName, editTextKnownDiagnosis, editTextCurrentDiagnosis;
    private Button buttonSave;
    private TextView textDate, textTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        // Initialize UI elements
        editTextPatientName = findViewById(R.id.editTextPatientName);
        editTextKnownDiagnosis = findViewById(R.id.editTextKnownDiagnosis);
        editTextCurrentDiagnosis = findViewById(R.id.editTextCurrentDiagnosis);
        buttonSave = findViewById(R.id.buttonSave);
        textDate = findViewById(R.id.textDate);
        textTime = findViewById(R.id.textTime);

        // Display current date and time
        setCurrentDateTime();

        // Handle save button click
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save patient data logic (To be implemented)
                finish(); // Go back to Main Screen
            }
        });
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
