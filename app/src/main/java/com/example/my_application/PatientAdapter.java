package com.example.my_application;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<PatientModel> patientList;
    private List<PatientModel> originalList;
    private Context context;

    public PatientAdapter(List<PatientModel> patientList) {
        this.patientList = patientList;
        this.originalList = new ArrayList<>(patientList);
        this.context = null; // Will be set in onCreateViewHolder
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientModel patient = patientList.get(position);
        holder.textName.setText(patient.getName());
        
        // Set the known diagnosis
        if (patient.getKnownDiagnosis() != null && !patient.getKnownDiagnosis().isEmpty()) {
            holder.textKnownDiagnosis.setText("Known Diagnosis: " + patient.getKnownDiagnosis());
            holder.textKnownDiagnosis.setVisibility(View.VISIBLE);
        } else {
            holder.textKnownDiagnosis.setVisibility(View.GONE);
        }
        
        // Set the current diagnosis
        if (patient.getCurrentDiagnosis() != null && !patient.getCurrentDiagnosis().isEmpty()) {
            holder.textCurrentDiagnosis.setText("Current Diagnosis: " + patient.getCurrentDiagnosis());
            holder.textCurrentDiagnosis.setVisibility(View.VISIBLE);
        } else {
            holder.textCurrentDiagnosis.setVisibility(View.GONE);
        }
        
        // Set click listener to open patient details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientDetailActivity.class);
            intent.putExtra("patientName", patient.getName());
            intent.putExtra("knownDiagnosis", patient.getKnownDiagnosis());
            intent.putExtra("currentDiagnosis", patient.getCurrentDiagnosis());
            intent.putExtra("date", patient.getDate());
            intent.putExtra("time", patient.getTime());
            
            // Add image URLs if available
            if (patient.getImageUrls() != null && !patient.getImageUrls().isEmpty()) {
                intent.putStringArrayListExtra("imageUrls", new ArrayList<>(patient.getImageUrls()));
            }
            
            // Add thumbnails if available
            if (patient.getThumbnails() != null && !patient.getThumbnails().isEmpty()) {
                intent.putStringArrayListExtra("thumbnails", new ArrayList<>(patient.getThumbnails()));
            }
            
            context.startActivity(intent);
        });
        
        // Set delete button listener
        holder.btnDeletePatient.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onPatientDelete(patient, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    // Method to update the list when searching
    public void updateList(List<PatientModel> newList) {
        patientList.clear();
        patientList.addAll(newList);
        notifyDataSetChanged();
    }
    
    // Method to filter the list based on search query
    public void filter(String query) {
        if (query.isEmpty()) {
            // If search query is empty, restore the original list
            patientList.clear();
            patientList.addAll(originalList);
        } else {
            // Filter the list based on patient name
            List<PatientModel> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();
            
            for (PatientModel patient : originalList) {
                if (patient.getName().toLowerCase().contains(lowerCaseQuery) ||
                    (patient.getKnownDiagnosis() != null && patient.getKnownDiagnosis().toLowerCase().contains(lowerCaseQuery)) ||
                    (patient.getCurrentDiagnosis() != null && patient.getCurrentDiagnosis().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(patient);
                }
            }
            
            patientList.clear();
            patientList.addAll(filteredList);
        }
        
        notifyDataSetChanged();
    }
    
    // Method to get the original list for refreshing data
    public List<PatientModel> getOriginalList() {
        return originalList;
    }

    // Method to keep originalList and patientList in sync
    public void setPatients(List<PatientModel> newList) {
        patientList.clear();
        patientList.addAll(newList);
        originalList.clear();
        originalList.addAll(newList);
        notifyDataSetChanged();
    }

    // Add interface for delete callback
    public interface OnPatientDeleteListener {
        void onPatientDelete(PatientModel patient, int position);
    }

    private OnPatientDeleteListener deleteListener;

    public void setOnPatientDeleteListener(OnPatientDeleteListener listener) {
        this.deleteListener = listener;
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        TextView textKnownDiagnosis;
        TextView textCurrentDiagnosis;
        ImageButton btnDeletePatient;

        public PatientViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textKnownDiagnosis = itemView.findViewById(R.id.textKnownDiagnosis);
            textCurrentDiagnosis = itemView.findViewById(R.id.textCurrentDiagnosis);
            btnDeletePatient = itemView.findViewById(R.id.btnDeletePatient);
        }
    }
}
