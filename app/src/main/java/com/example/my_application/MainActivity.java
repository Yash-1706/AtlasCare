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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerViewPatients;
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddPatient;
    private FirebaseHandler firebaseHandler;
    private TextView tvConnectionStatus;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    private boolean isConnected = true;
    private boolean isFirstLoad = true;

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

        // Initialize Firebase
        firebaseHandler = new FirebaseHandler();

        // Initialize the list of patients
        List<PatientModel> patientList = new ArrayList<>();

        // Create an adapter and set it to the RecyclerView
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
                            // Find patient ID (assuming PatientModel has a getId or similar method)
                            String patientId = patient.getFirebaseKey();
                            if (patientId == null || patientId.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Cannot delete: Patient ID not found!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setMessage("Deleting patient...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            firebaseHandler.deletePatientById(patientId, new FirebaseHandler.FirebaseCallback() {
                                @Override
                                public void onSuccess() {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Patient deleted successfully", Toast.LENGTH_SHORT).show();
                                    // Reload patients from Firebase
                                    loadPatientsFromFirebase(adapter, adapter.getOriginalList());
                                }
                                @Override
                                public void onFailure(String error) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Failed to delete: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
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

        // Load patients from Firebase
        loadPatientsFromFirebase(adapter, patientList);

        // Set up FAB click listener
        fabAddPatient.setOnClickListener(view -> {
            if (!isConnected) {
                Toast.makeText(MainActivity.this, "No internet connection. Cannot add patients offline.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, AddPatientActivity.class);
            startActivity(intent);
        });
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
                    Log.d(TAG, "Network connection available");
                    // Refresh data when connection is restored
                    PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
                    if (adapter != null) {
                        List<PatientModel> patientList = adapter.getOriginalList();
                        loadPatientsFromFirebase(adapter, patientList);
                    }
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runOnUiThread(() -> {
                    isConnected = false;
                    updateConnectionUI();
                    Log.d(TAG, "Network connection lost");
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
                Log.e(TAG, "Error unregistering network callback: " + e.getMessage());
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the patient list when returning to the activity, but only if needed
        PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
        if (adapter != null) {
            List<PatientModel> patientList = adapter.getOriginalList();
            if (isFirstLoad || patientList.isEmpty()) {
                loadPatientsFromFirebase(adapter, patientList);
                isFirstLoad = false;
            }
        }
    }

    private void loadPatientsFromFirebase(PatientAdapter adapter, List<PatientModel> patientList) {
        // Don't attempt to load if there's no connection
        if (!isConnected) {
            Toast.makeText(this, "No internet connection. Cannot load patients.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading patients...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Clear existing data
        patientList.clear();
        adapter.notifyDataSetChanged();

        // Load patients from Firebase
        firebaseHandler.getAllPatients(new FirebaseHandler.FirebaseDataCallback() {
            @Override
            public void onSuccess(List<PatientModel> patients) {
                progressDialog.dismiss();
                if (patients.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No patients found", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.setPatients(patients);
                    // Only show toast on first load or if explicitly refreshing
                    if (progressDialog.isShowing()) {
                        Toast.makeText(MainActivity.this, patients.size() + " patients loaded", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to load patients: " + error);
            }
        });
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

        // Clear data from Firebase
        firebaseHandler.clearAllData(new FirebaseHandler.FirebaseCallback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "All data cleared successfully", Toast.LENGTH_SHORT).show();
                // Refresh the patient list
                PatientAdapter adapter = (PatientAdapter) recyclerViewPatients.getAdapter();
                if (adapter != null) {
                    List<PatientModel> patientList = adapter.getOriginalList();
                    patientList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to clear data: " + error);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        super.onBackPressed();
        finishAffinity(); // Closes all activities in the task
    }
}
