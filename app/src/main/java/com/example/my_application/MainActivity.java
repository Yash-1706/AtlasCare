package com.example.my_application;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerViewPatients;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddPatient;
    private TextView tvConnectionStatus;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    private boolean isConnected = true;
    private boolean isFirstLoad = true;
    private boolean showPatientsLoadedToast = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewPatients = findViewById(R.id.recyclerViewPatients);
        searchView = findViewById(R.id.searchView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabAddPatient = findViewById(R.id.fabAddPatient);
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus);

        recyclerViewPatients.setLayoutManager(new LinearLayoutManager(this));

        // Fetch patients from Room DB
        List<PatientEntity> patientEntities = AppDatabase.getInstance(this).patientDao().getAllPatients();
        List<PatientModel> patientList = new ArrayList<>();
        for (PatientEntity entity : patientEntities) {
            patientList.add(entityToModel(entity));
        }

        PatientAdapter adapter = new PatientAdapter(patientList);
        recyclerViewPatients.setAdapter(adapter);

        // Set up delete listener for patient adapter
        adapter.setOnPatientDeleteListener(new PatientAdapter.OnPatientDeleteListener() {
            @Override
            public void onPatientDelete(PatientModel patient, int position) {
                // Show confirmation dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Patient")
                        .setMessage("Are you sure you want to delete this patient?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Delete from DB
                            PatientEntity entity = modelToEntity(patient);
                            AppDatabase.getInstance(MainActivity.this).patientDao().deletePatient(entity);
                            loadPatients(adapter);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        // Prevent double population by suppressing default SearchView close behavior
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Do nothing, let onQueryTextChange handle restoring the list
                return true;
            }
        });

        // Set up bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.nav_statistics) {
                // Navigate to statistics screen
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Set up network connectivity monitoring
        setupNetworkCallback();

        // Set up FAB click listener
        fabAddPatient.setOnClickListener(v -> {
            if (!isConnected) {
                Toast.makeText(MainActivity.this, "No internet connection. Cannot add patients offline.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, AddPatientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
        if (adapter != null) {
            List<PatientEntity> patientEntities = AppDatabase.getInstance(this).patientDao().getAllPatients();
            List<PatientModel> updatedList = new ArrayList<>();
            for (PatientEntity entity : patientEntities) {
                updatedList.add(entityToModel(entity));
            }
            adapter.setPatients(updatedList);
        }
    }

    private void setupNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Check initial connection state
        isConnected = isNetworkAvailable();
        updateConnectionUI();
        
        // Create network callback
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                // Run on UI thread since callback might be called from a different thread
                runOnUiThread(() -> {
                    isConnected = true;
                    updateConnectionUI();
                    // Refresh data when connection is restored
                    PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
                    if (adapter != null) {
                        loadPatients(adapter);
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runOnUiThread(() -> {
                    isConnected = false;
                    updateConnectionUI();
                });
            }
        };

        // Register the callback
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private boolean isNetworkAvailable() {
        if (connectivityManager == null) return false;
        
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void updateConnectionUI() {
        if (isConnected) {
            tvConnectionStatus.setVisibility(View.GONE);
        } else {
            tvConnectionStatus.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister network callback to prevent memory leaks
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                // Optionally, handle error silently
            }
        }
    }

    private void loadPatients(PatientAdapter adapter) {
        if (!isConnected) {
            Toast.makeText(this, "No internet connection. Cannot load patients.", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading patients...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        List<PatientEntity> patientEntities = AppDatabase.getInstance(this).patientDao().getAllPatients();
        List<PatientModel> patientList = new ArrayList<>();
        for (PatientEntity entity : patientEntities) {
            patientList.add(entityToModel(entity));
        }
        adapter.setPatients(patientList);
        progressDialog.dismiss();
    }

    private PatientModel entityToModel(PatientEntity entity) {
        PatientModel model = new PatientModel(entity.name, entity.knownDiagnosis, entity.currentDiagnosis, entity.date, entity.time);
        model.setImageUrls(entity.imageUrls);
        return model;
    }

    private PatientEntity modelToEntity(PatientModel model) {
        PatientEntity entity = new PatientEntity();
        entity.name = model.getName();
        entity.knownDiagnosis = model.getKnownDiagnosis();
        entity.currentDiagnosis = model.getCurrentDiagnosis();
        entity.date = model.getDate();
        entity.time = model.getTime();
        entity.imageUrls = model.getImageUrls();
        return entity;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_data) {
            // Show confirmation dialog before clearing data
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to clear all data? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        clearAllData();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearAllData() {
        // Don't attempt to clear if there's no connection
        if (!isConnected) {
            Toast.makeText(this, "No internet connection. Cannot clear data.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Clearing data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Clear data from DB
        AppDatabase.getInstance(this).patientDao().clearAllPatients();

        // Refresh the patient list
        PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
        if (adapter != null) {
            List<PatientModel> patientList = adapter.getOriginalList();
            patientList.clear();
            adapter.notifyDataSetChanged();
        }
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        super.onBackPressed();
        finishAffinity(); // Closes all activities in the task
    }
}
