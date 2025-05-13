package com.example.my_application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PatientImageAdapter extends RecyclerView.Adapter<PatientImageAdapter.ImageViewHolder> {

    private static final String TAG = "PatientImageAdapter";
    private List<String> imageUrls;
    private List<String> fullImageUrls;
    private Context context;

    public PatientImageAdapter(List<String> imageUrls, List<String> fullImageUrls, Context context) {
        this.imageUrls = imageUrls;
        this.fullImageUrls = fullImageUrls;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Log.d(TAG, "Loading image at position " + position + ": " + (imageUrl != null ? imageUrl.substring(0, Math.min(50, imageUrl.length())) + "..." : "null"));
        
        // Show loading indicator
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.patientImage.setVisibility(View.INVISIBLE);
        
        if (imageUrl == null) {
            showErrorImage(holder);
            Log.e(TAG, "Null image URL at position " + position);
            return;
        }
        
        // Only use Glide for Drive links (not base64)
        if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            Glide.with(context)
                .load(imageUrl)
                .into(holder.patientImage);
            holder.patientImage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        } else if (imageUrl != null && imageUrl.startsWith("data:image/")) {
            try {
                // Parse the Base64 data
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                // Convert Base64 to Bitmap
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    holder.patientImage.setImageBitmap(bitmap);
                    holder.patientImage.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                } else {
                    showErrorImage(holder);
                }
            } catch (Exception e) {
                showErrorImage(holder);
            }
        } else {
            showErrorImage(holder);
        }
        
        // Set click listener to show full-screen image
        holder.patientImage.setOnClickListener(v -> {
            if (fullImageUrls != null && position < fullImageUrls.size()) {
                String fullImage = fullImageUrls.get(position);
                // Show full image in dialog
                showFullImageDialog(fullImage);
            } else {
                Toast.makeText(context, "Full image not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showErrorImage(ImageViewHolder holder) {
        holder.patientImage.setImageResource(R.drawable.ic_launcher_background);
        holder.patientImage.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.GONE);
    }

    private void showFullImageDialog(String base64Image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_full_image, null);
        ImageView imageView = dialogView.findViewById(R.id.fullImageView);
        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);
        Bitmap bitmap = base64ToBitmap(base64Image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        btnBack.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Bitmap base64ToBitmap(String base64Image) {
        try {
            // Parse the Base64 data
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding Base64 image: " + e.getMessage());
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView patientImage;
        ProgressBar progressBar;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            patientImage = itemView.findViewById(R.id.ivPatientImage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
