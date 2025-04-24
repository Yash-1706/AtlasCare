package com.example.my_application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.VisitViewHolder> {
    private List<VisitModel> visitList;

    public VisitAdapter(List<VisitModel> visitList) {
        this.visitList = visitList;
    }

    @NonNull
    @Override
    public VisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visit, parent, false);
        return new VisitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitViewHolder holder, int position) {
        VisitModel visit = visitList.get(position);
        holder.tvVisitDate.setText(visit.getDate());
        holder.tvVisitDiagnosis.setText(visit.getDiagnosis());
        holder.tvVisitNotes.setText(visit.getNotes() != null ? visit.getNotes() : "");
        // TODO: Set up edit button click if needed
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    static class VisitViewHolder extends RecyclerView.ViewHolder {
        TextView tvVisitDate, tvVisitDiagnosis, tvVisitNotes;
        ImageButton btnEditVisit;
        VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
            tvVisitDiagnosis = itemView.findViewById(R.id.tvVisitDiagnosis);
            tvVisitNotes = itemView.findViewById(R.id.tvVisitNotes);
            btnEditVisit = itemView.findViewById(R.id.btnEditVisit);
        }
    }
}
