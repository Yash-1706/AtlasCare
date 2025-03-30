package com.example.my_application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPatients;
    private PatientAdapter patientAdapter;
    private List<PatientModel> patientList;
    private List<PatientModel> filteredList;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddPatient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        searchView = findViewById(R.id.searchView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        FloatingActionButton fabAddPatient = findViewById(R.id.fabAddPatient);

        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(this));

        // Dummy data for patient list (Replace with real data later)
        patientList = new ArrayList<>();
        patientList.add(new PatientModel("John Doe", "Diabetes", "High Blood Pressure"));
        patientList.add(new PatientModel("Jane Smith", "Asthma", "Cold and Cough"));
        patientList.add(new PatientModel("Mike Johnson", "Heart Disease", "Chest Pain"));

        patientAdapter = new PatientAdapter(patientList);
        recyclerViewPatients.setAdapter(patientAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("AtlasCare");
        }


        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPatients(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPatients(newText);
                return false;
            }
        });

        // Bottom Navigation Selection
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    return true; // Stay on home screen
                } else if (item.getItemId() == R.id.nav_statistics) {
                    Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // Floating Action Button Click - Add New Patient
        fabAddPatient.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPatientActivity.class);
            startActivity(intent);
        });
    }

    // Function to filter patient list based on search query
    private void filterPatients(String query) {
        filteredList = new ArrayList<>();
        for (PatientModel patient : patientList) {
            if (patient.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(patient);
            }
        }
        patientAdapter.updateList(filteredList);
    }

    @Override
    public void onBackPressed() {
        // Exit the app when the back button is pressed
        super.onBackPressed();
        finishAffinity(); // Closes all activities in the task
    }

}
