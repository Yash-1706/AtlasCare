package com.example.my_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PatientAdapter adapter;
    private List<PatientModel> patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AtlasCare");

        recyclerView = findViewById(R.id.recyclerViewPatients);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddPatient = findViewById(R.id.fabAddPatient);
        fabAddPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

        // Load Patients
        loadPatients();

        // SearchView for filtering patients
        SearchView searchView = findViewById(R.id.searchPatient);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterList(newText);
                return false;
            }
        });
    }

    private void loadPatients() {
        patientList = new ArrayList<>();
        patientList.add(new PatientModel("John Doe", "12345"));
        patientList.add(new PatientModel("Jane Smith", "67890"));
        patientList.add(new PatientModel("Alice Johnson", "54321"));

        adapter = new PatientAdapter(patientList);
        recyclerView.setAdapter(adapter);
    }
}
