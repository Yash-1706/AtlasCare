package com.example.my_application;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "StatisticsActivity";
    private Button selectDateButton;
    private RecyclerView patientRecyclerView;
    private PatientAdapter patientAdapter;
    private List<PatientModel> patientList = new ArrayList<>();
    private TextView todayPatientsCount, monthPatientsCount, selectedDatePatients;
    private FirebaseHandler firebaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        selectDateButton = findViewById(R.id.selectDateButton);
        patientRecyclerView = findViewById(R.id.patientRecyclerView);
        todayPatientsCount = findViewById(R.id.todayPatientsCount);
        monthPatientsCount = findViewById(R.id.monthPatientsCount);
        selectedDatePatients = findViewById(R.id.selectedDatePatients);
        
        // Initialize Firebase handler
        firebaseHandler = new FirebaseHandler();

        // Initialize RecyclerView
        patientRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientAdapter = new PatientAdapter(patientList);
        patientRecyclerView.setAdapter(patientAdapter);

        // Load all patients from Firebase
        loadAllPatientsFromFirebase();

        // Date Picker Feature
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_statistics);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadAllPatientsFromFirebase();
    }
    
    private void loadAllPatientsFromFirebase() {
        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading patients from cloud...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Clear existing list to prevent duplicates
        patientList.clear();
        
        firebaseHandler.getAllPatients(new FirebaseHandler.FirebaseDataCallback() {
            @Override
            public void onSuccess(List<PatientModel> patients) {
                progressDialog.dismiss();
                
                // Clear and update the list
                patientList.clear();
                
                if (patients != null && !patients.isEmpty()) {
                    patientList.addAll(patients);
                    Log.d(TAG, "Loaded " + patients.size() + " patients from Firebase");
                    
                    // Log the patients for debugging
                    for (PatientModel patient : patients) {
                        Log.d(TAG, "Patient: " + patient.getName() + ", Date: " + patient.getDate() + 
                              ", KnownDiag: " + patient.getKnownDiagnosis() + 
                              ", CurrentDiag: " + patient.getCurrentDiagnosis());
                    }
                    
                    // Update UI with statistics
                    updatePatientCounts();
                } else {
                    Log.d(TAG, "No patients found in Firebase");
                    clearStatistics();
                }
                
                // Always notify adapter to refresh the view
                patientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Log.e(TAG, "Failed to load patients from Firebase: " + error);
                Toast.makeText(StatisticsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                
                // Clear list in case of error to avoid showing stale data
                patientList.clear();
                patientAdapter.notifyDataSetChanged();
                clearStatistics();
            }
        });
    }
    
    private void clearStatistics() {
        // Clear all stats in case of error or no data
        monthPatientsCount.setText("Total patients visited this month: 0");
        todayPatientsCount.setText("Total patients visited today: 0");
        selectedDatePatients.setText("Patients visited on selected date: 0");
    }

    private void updatePatientCounts() {
        // Count today's patients
        int todayCount = countPatientsForToday();
        // Count this month's patients
        int monthCount = countPatientsForMonth();

        Log.d(TAG, "Today's patient count: " + todayCount);
        Log.d(TAG, "Month's patient count: " + monthCount);
        
        monthPatientsCount.setText("Total patients visited this month: " + monthCount);
        todayPatientsCount.setText("Total patients visited today: " + todayCount);
    }
    
    private int countPatientsForToday() {
        // Get today's date in the same format as stored in Firebase (dd/MM/yyyy)
        Calendar calendar = Calendar.getInstance();
        String todayDate = String.format("%02d/%02d/%04d", 
                                       calendar.get(Calendar.DAY_OF_MONTH),
                                       calendar.get(Calendar.MONTH) + 1,
                                       calendar.get(Calendar.YEAR));
        
        int count = 0;
        for (PatientModel patient : patientList) {
            if (patient.getDate() != null && patient.getDate().equals(todayDate)) {
                count++;
            }
        }
        return count;
    }
    
    private int countPatientsForMonth() {
        // Get current month/year pattern (MM/yyyy)
        Calendar calendar = Calendar.getInstance();
        String monthYearPattern = String.format("%02d/%04d", 
                                             calendar.get(Calendar.MONTH) + 1,
                                             calendar.get(Calendar.YEAR));
        
        int count = 0;
        for (PatientModel patient : patientList) {
            // Check if patient date contains the current month/year pattern
            if (patient.getDate() != null && patient.getDate().contains(monthYearPattern)) {
                count++;
            }
        }
        return count;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format date as dd/MM/yyyy to match database format
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, (month + 1), year);
                    Log.d(TAG, "Selected date: " + selectedDate);
                    
                    int patientCount = countPatientsForDate(selectedDate);
                    Log.d(TAG, "Patient count for " + selectedDate + ": " + patientCount);
                    
                    selectedDatePatients.setText("Patients visited on selected date: " + patientCount);
                    filterPatientsByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private int countPatientsForDate(String selectedDate) {
        int count = 0;
        for (PatientModel patient : patientList) {
            if (patient.getDate() != null && patient.getDate().equals(selectedDate)) {
                count++;
            }
        }
        return count;
    }

    private void filterPatientsByDate(String selectedDate) {
        try {
            Log.d(TAG, "Filtering patients for date: " + selectedDate);
            Log.d(TAG, "Total patients before filtering: " + patientList.size());
            
            List<PatientModel> filteredList = new ArrayList<>();
            for (PatientModel patient : patientList) {
                Log.d(TAG, "Checking patient: " + patient.getName() + " with date: " + patient.getDate());
                if (patient.getDate() != null && patient.getDate().equals(selectedDate)) {
                    filteredList.add(patient);
                    Log.d(TAG, "Added patient to filtered list: " + patient.getName());
                }
            }
            
            Log.d(TAG, "Found " + filteredList.size() + " patients for date " + selectedDate);
            
            // Update adapter with filtered list
            patientAdapter.updateList(filteredList);
            
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No patients found for: " + selectedDate, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Showing " + filteredList.size() + " patients for: " + selectedDate, Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error filtering patients: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error filtering patients", Toast.LENGTH_SHORT).show();
        }
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
