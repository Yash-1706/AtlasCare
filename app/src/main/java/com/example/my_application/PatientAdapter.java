package com.example.my_application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private List<PatientModel> patientList;
    private List<PatientModel> fullList; // For search filtering

    public PatientAdapter(List<PatientModel> patientList) {
        this.patientList = new ArrayList<>(patientList);
        this.fullList = new ArrayList<>(patientList);
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientModel patient = patientList.get(position);
        holder.tvPatientName.setText(patient.getName());
        holder.tvCaseNumber.setText("Case No: " + patient.getCaseNumber());
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvCaseNumber;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvCaseNumber = itemView.findViewById(R.id.tvCaseNumber);
        }
    }

    // Search Filter
    public void filterList(String query) {
        patientList.clear();
        if (query.isEmpty()) {
            patientList.addAll(fullList);
        } else {
            for (PatientModel patient : fullList) {
                if (patient.getName().toLowerCase().contains(query.toLowerCase())) {
                    patientList.add(patient);
                }
            }
        }
        notifyDataSetChanged();
    }
}
