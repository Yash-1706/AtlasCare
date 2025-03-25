package com.example.my_application;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {
    private String selectedBloodGroup;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        EditText etPatientName = findViewById(R.id.etPatientName);
        EditText etCaseNumber = findViewById(R.id.etCaseNumber);
        EditText etAge = findViewById(R.id.etAge);
        TextView tvSelectedDate = findViewById(R.id.tvSelectedDate);
        Button btnPickDate = findViewById(R.id.btnPickDate);
        Spinner spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        EditText etPhoneNumber = findViewById(R.id.etPhoneNumber);
        Button btnSave = findViewById(R.id.btnSave);

        // Set up the Blood Group Spinner (Dropdown)
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bloodGroups);
        spinnerBloodGroup.setAdapter(adapter);

        spinnerBloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBloodGroup = bloodGroups[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBloodGroup = "";
            }
        });

        // Set up Date Picker
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SecondActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                tvSelectedDate.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Save button click listener
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientName = etPatientName.getText().toString();
                String caseNumber = etCaseNumber.getText().toString();
                String age = etAge.getText().toString();
                String phoneNumber = etPhoneNumber.getText().toString();

                if (patientName.isEmpty() || caseNumber.isEmpty() || age.isEmpty() ||
                        selectedBloodGroup.equals("Select Blood Group") || phoneNumber.isEmpty() || selectedDate == null) {
                    Toast.makeText(SecondActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show confirmation
                Toast.makeText(SecondActivity.this, "Patient Details Saved", Toast.LENGTH_SHORT).show();

                // Redirect to MainActivity
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
