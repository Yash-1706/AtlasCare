package com.example.my_application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<PatientModel> patientList;

    public PatientAdapter(List<PatientModel> patientList) {
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        PatientModel patient = patientList.get(position);
        holder.textName.setText(patient.getName());
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    // Method to update the list when searching
    public void updateList(List<PatientModel> newList) {
        patientList = newList;
        notifyDataSetChanged();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView textName;

        public PatientViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
        }
    }
}
