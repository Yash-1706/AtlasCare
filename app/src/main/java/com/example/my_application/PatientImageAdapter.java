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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PatientImageAdapter extends RecyclerView.Adapter<PatientImageAdapter.ImageViewHolder> {

    private static final String TAG = "PatientImageAdapter";
    private List<String> imageUrls;
    private Context context;

    public PatientImageAdapter(List<String> imageUrls, Context context) {
        this.imageUrls = imageUrls;
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
        
        // Check if it's a direct Base64 image
        if (imageUrl.startsWith("data:image/")) {
            try {
                // Parse the Base64 data
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                Log.d(TAG, "Processing direct Base64 image, length: " + base64Data.length());
                
                // Convert Base64 to Bitmap
                Bitmap bitmap = base64ToBitmap(base64Data);
                if (bitmap != null) {
                    // Load the bitmap into the ImageView
                    holder.patientImage.setImageBitmap(bitmap);
                    holder.patientImage.setVisibility(View.VISIBLE);
                    holder.progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Successfully loaded direct Base64 image");
                } else {
                    // Show error image
                    showErrorImage(holder);
                    Log.e(TAG, "Failed to decode direct Base64 image");
                }
            } catch (Exception e) {
                showErrorImage(holder);
                Log.e(TAG, "Error processing direct Base64 image: " + e.getMessage());
            }
        }
        // Check if it's a Firebase Database reference
        else if (imageUrl.startsWith("firebase://")) {
            // Parse the path from the custom URL
            // Format is: firebase://patient_images/PATIENT_ID/image_INDEX
            String path = imageUrl.substring("firebase://".length());
            Log.d(TAG, "Firebase path: " + path);
            
            // Get the image from Firebase Database
            DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference().child(path);
            Log.d(TAG, "Attempting to load from database reference: " + imageRef.toString());
            
            imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Database snapshot exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        String base64Image = snapshot.getValue(String.class);
                        Log.d(TAG, "Base64 image data length: " + (base64Image != null ? base64Image.length() : 0));
                        if (base64Image != null && !base64Image.isEmpty()) {
                            // Convert Base64 to Bitmap
                            Bitmap bitmap = base64ToBitmap(base64Image);
                            if (bitmap != null) {
                                Log.d(TAG, "Successfully decoded bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                                // Load the bitmap into the ImageView
                                holder.patientImage.setImageBitmap(bitmap);
                                holder.patientImage.setVisibility(View.VISIBLE);
                                holder.progressBar.setVisibility(View.GONE);
                                Log.d(TAG, "Successfully loaded image from Firebase");
                            } else {
                                // Show error image
                                showErrorImage(holder);
                                Log.e(TAG, "Failed to decode Base64 image");
                            }
                        } else {
                            // Show error image
                            showErrorImage(holder);
                            Log.e(TAG, "Empty Base64 image data");
                        }
                    } else {
                        // Show error image
                        showErrorImage(holder);
                        Log.e(TAG, "Image not found in database: " + path);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Show error image
                    showErrorImage(holder);
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        } else {
            // Regular URL, use Glide
            Log.d(TAG, "Using Glide to load regular URL");
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // Use a placeholder while loading
                    .error(R.drawable.ic_launcher_background) // Use an error image if loading fails
                    .into(holder.patientImage);
            holder.patientImage.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }
        
        // Set click listener to show full-screen image
        holder.patientImage.setOnClickListener(v -> {
            Toast.makeText(context, "Image " + (position + 1), Toast.LENGTH_SHORT).show();
            // Could implement a full-screen image viewer here
        });
    }
    
    private void showErrorImage(ImageViewHolder holder) {
        holder.patientImage.setImageResource(R.drawable.ic_launcher_background);
        holder.patientImage.setVisibility(View.VISIBLE);
        holder.progressBar.setVisibility(View.GONE);
    }

    private Bitmap base64ToBitmap(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
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
