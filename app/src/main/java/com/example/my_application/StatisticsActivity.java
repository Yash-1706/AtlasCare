package com.example.my_application;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
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

    private Button selectDateButton;
    private RecyclerView patientRecyclerView;
    private PatientAdapter patientAdapter;
    private List<PatientModel> patientList = new ArrayList<>();
    private TextView todayPatientsCount, monthPatientsCount, selectedDatePatients;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        selectDateButton = findViewById(R.id.selectDateButton);
        patientRecyclerView = findViewById(R.id.patientRecyclerView);
        todayPatientsCount = findViewById(R.id.todayPatientsCount);
        monthPatientsCount = findViewById(R.id.monthPatientsCount);
        selectedDatePatients = findViewById(R.id.selectedDatePatients);
        databaseHelper = new DatabaseHelper(this);

        // Initialize RecyclerView
        patientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList);
        patientRecyclerView.setAdapter(patientAdapter);

        // Update patient count on UI
        updatePatientCounts();

        // Date Picker Feature
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void updatePatientCounts() {
        int todayCount = databaseHelper.getPatientCountForToday();
        int monthCount = databaseHelper.getPatientCountForMonth();

        monthPatientsCount.setText("Total patients visited this month: " + monthCount);
        todayPatientsCount.setText("Total patients visited today: " + todayCount);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    selectedDatePatients.setText("Patients visited on " + selectedDate + ": " + getPatientCountForDate(selectedDate));
                    filterPatientsByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private int getPatientCountForDate(String date) {
        return databaseHelper.getPatientCountForDate(date);
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
