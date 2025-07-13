package com.example.my_application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import java.io.File;
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

        holder.progressBar.setVisibility(View.VISIBLE);
        holder.patientImage.setVisibility(View.INVISIBLE);

        if (imageUrl == null) {
            showErrorImage(holder);
            Log.e(TAG, "Null image URL at position " + position);
            return;
        }

        try {
            File imgFile = new File(imageUrl);
            if (imgFile.exists()) {
                Log.d(TAG, "Attempting to load local file: " + imgFile.getAbsolutePath());
                Glide.with(context)
                    .load(Uri.fromFile(imgFile))
                    .thumbnail(0.2f)
                    .fitCenter()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.patientImage);
                holder.progressBar.setVisibility(View.GONE);
                holder.patientImage.setVisibility(View.VISIBLE);
            } else if (imageUrl.startsWith("data:image/")) {
                // Handle Base64 inline images if any
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                holder.patientImage.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
                holder.progressBar.setVisibility(View.GONE);
                holder.patientImage.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "File does not exist: " + imageUrl);
                Toast.makeText(context, "Image file not found: " + imageUrl, Toast.LENGTH_SHORT).show();
                Glide.with(context)
                    .load(imageUrl)
                    .thumbnail(0.2f)
                    .fitCenter()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.patientImage);
                holder.progressBar.setVisibility(View.GONE);
                holder.patientImage.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            showErrorImage(holder);
            Log.e(TAG, "Error displaying image: " + e.getMessage());
            Toast.makeText(context, "Error displaying image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        holder.patientImage.setOnClickListener(v -> {
            String fullImagePath = imageUrls.get(position);
            showFullImageDialog(fullImagePath);
        });
    }

    private void showErrorImage(ImageViewHolder holder) {
        holder.patientImage.setImageResource(R.drawable.ic_launcher_background);
        holder.patientImage.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.GONE);
    }

    // Show full-size image in a dialog from local file or URL
    private void showFullImageDialog(String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_full_image, null);
        ImageView imageView = dialogView.findViewById(R.id.fullImageView);
        ImageButton btnBack = dialogView.findViewById(R.id.btnBack);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Glide.with(context)
                .load(Uri.fromFile(imgFile))
                .fitCenter()
                .into(imageView);
        } else if (imagePath.startsWith("data:image/")) {
            try {
                String base64Data = imagePath.substring(imagePath.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            Glide.with(context)
                .load(imagePath)
                .fitCenter()
                .into(imageView);
        }
        btnBack.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
