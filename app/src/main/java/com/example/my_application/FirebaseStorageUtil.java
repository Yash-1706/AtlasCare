// This file is now unused and all Firebase imports and usages have been removed.
// You may safely delete this file from your project.

package com.example.my_application;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseStorageUtil {
    private static final String TAG = "FirebaseStorageUtil";
    
    /**
     * Resize a bitmap to have a maximum dimension of maxSize
     */
    private static Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap; // No need to resize
        }
        
        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    /**
     * Convert a bitmap to Base64 string
     */
    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    
    /**
     * Callback interface for operations
     */
    public interface FirebaseCallback {
        void onSuccess(String imageReference);
        void onFailure(String error);
    }
}
