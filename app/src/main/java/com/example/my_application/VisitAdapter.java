package com.example.my_application;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.VisitViewHolder> {
    private List<VisitModel> visitList;
    private List<Boolean> expandedStates = new ArrayList<>();

    public VisitAdapter(List<VisitModel> visitList) {
        this.visitList = visitList;
        expandedStates.clear();
        for (int i = 0; i < visitList.size(); i++) expandedStates.add(false);
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
        // Set visit number label
        String visitLabel;
        switch (position) {
            case 0: visitLabel = "1st visit"; break;
            case 1: visitLabel = "2nd visit"; break;
            case 2: visitLabel = "3rd visit"; break;
            default: visitLabel = (position+1) + "th visit"; break;
        }
        holder.tvVisitNumberLabel.setText(visitLabel);
        holder.tvVisitDate.setText("Date: " + visit.getDate());
        // Fix: ensure expandedStates is always large enough
        while (expandedStates.size() <= position) expandedStates.add(false);
        boolean expanded = expandedStates.get(position);
        holder.detailsLayout.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.tvVisitNumberLabel.setOnClickListener(v -> {
            expandedStates.set(position, !expandedStates.get(position));
            notifyItemChanged(position);
        });
        // Move all details below this line into holder.detailsLayout
        holder.tvVisitDiagnosis.setVisibility(View.VISIBLE);
        if (visit.getDiagnosis() != null && !visit.getDiagnosis().isEmpty()) {
            holder.tvVisitDiagnosis.setText(visit.getDiagnosis());
        } else {
            holder.tvVisitDiagnosis.setText("-");
        }
        if (visit.getTime() != null && !visit.getTime().isEmpty()) {
            holder.tvVisitTime.setText("Time: " + visit.getTime());
            holder.tvVisitTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvVisitTime.setText("Time: -");
            holder.tvVisitTime.setVisibility(View.VISIBLE);
        }
        if (visit.getImageUrls() != null && !visit.getImageUrls().isEmpty()) {
            holder.rvVisitImages.setVisibility(View.VISIBLE);
            holder.tvPrescriptionPlaceholder.setVisibility(View.GONE);
            // Always set a LayoutManager before setting the adapter!
            if (holder.rvVisitImages.getLayoutManager() == null) {
                holder.rvVisitImages.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            }
            PatientImageAdapter imageAdapter = new PatientImageAdapter(
                visit.getImageUrls(),
                new ArrayList<>(),
                holder.itemView.getContext()
            );
            holder.rvVisitImages.setAdapter(imageAdapter);
        } else {
            holder.rvVisitImages.setVisibility(View.GONE);
            holder.tvPrescriptionPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull VisitViewHolder holder, int position, @NonNull List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        expandedStates.clear();
        for (int i = 0; i < visitList.size(); i++) expandedStates.add(false);
    }

    public void setVisitList(List<VisitModel> newList) {
        this.visitList = newList;
        expandedStates.clear();
        for (int i = 0; i < newList.size(); i++) expandedStates.add(false);
        notifyDataSetChanged();
    }

    static class VisitViewHolder extends RecyclerView.ViewHolder {
        TextView tvVisitNumberLabel, tvVisitDate, tvVisitDiagnosis, tvVisitTime, tvPrescriptionPlaceholder;
        RecyclerView rvVisitImages;
        View detailsLayout;
        ImageButton btnEditVisit;
        VisitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVisitNumberLabel = itemView.findViewById(R.id.tvVisitNumberLabel);
            tvVisitDate = itemView.findViewById(R.id.tvVisitDate);
            tvVisitDiagnosis = itemView.findViewById(R.id.tvVisitDiagnosis);
            tvVisitTime = itemView.findViewById(R.id.tvVisitTime);
            rvVisitImages = itemView.findViewById(R.id.rvVisitImages);
            tvPrescriptionPlaceholder = itemView.findViewById(R.id.tvPrescriptionPlaceholder);
            btnEditVisit = itemView.findViewById(R.id.btnEditVisit);
            detailsLayout = itemView.findViewById(R.id.visitDetailsLayout);
        }
    }
}
