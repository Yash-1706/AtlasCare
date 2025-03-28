package com.example.my_application;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private EditText searchPatient;
    private Button selectDateButton;
    private RecyclerView patientRecyclerView;
    private PatientAdapter patientAdapter;
    private List<PatientModel> patientList = new ArrayList<>(); // Replace with actual patient list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        searchPatient = findViewById(R.id.searchPatient);
        selectDateButton = findViewById(R.id.selectDateButton);
        patientRecyclerView = findViewById(R.id.patientRecyclerView);

        // Initialize RecyclerView
        patientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList); // Replace with actual data
        patientRecyclerView.setAdapter(patientAdapter);

        // Search Patient Feature
        searchPatient.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable s) {
                filterPatients(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Date Picker Feature
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void filterPatients(String query) {
        List<PatientModel> filteredList = new ArrayList<>();
        for (PatientModel patient : patientList) {
            if (patient.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(patient);
            }
        }
        patientAdapter.updateList(filteredList);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    filterPatientsByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void filterPatientsByDate(String selectedDate) {
        List<PatientModel> filteredList = new ArrayList<>();
        for (PatientModel patient : patientList) {
            if (patient.getDate().equals(selectedDate)) {
                filteredList.add(patient);
            }
        }
        patientAdapter.updateList(filteredList);
        Toast.makeText(this, "Showing results for: " + selectedDate, Toast.LENGTH_SHORT).show();
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (item.getItemId() == R.id.nav_statistics) {
            return true; // Already on this screen
        }
        return false;
    }
}
