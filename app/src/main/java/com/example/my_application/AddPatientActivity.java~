package com.example.my_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AddPatientActivity extends AppCompatActivity {

    private EditText editTextPatientName, editTextKnownDiagnosis, editTextCurrentDiagnosis;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        editTextPatientName = findViewById(R.id.editTextPatientName);
        editTextKnownDiagnosis = findViewById(R.id.editTextKnownDiagnosis);
        editTextCurrentDiagnosis = findViewById(R.id.editTextCurrentDiagnosis);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save patient data logic (To be implemented)
                finish(); // Go back to Main Screen
            }
        });
    }
}
