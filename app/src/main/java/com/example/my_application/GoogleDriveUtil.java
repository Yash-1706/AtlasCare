package com.example.my_application;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;

public class GoogleDriveUtil {
    private static final String TAG = "GoogleDriveUtil";
    private final Drive driveService;

    public GoogleDriveUtil(Context context, GoogleSignInAccount signInAccount) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton("https://www.googleapis.com/auth/drive.file"));
        credential.setSelectedAccount(signInAccount.getAccount());
        driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("AtlasCare").build();
    }

    public void uploadFile(java.io.File localFile, String mimeType, DriveUploadCallback callback) {
        new UploadTask(driveService, localFile, mimeType, callback).execute();
    }

    public interface DriveUploadCallback {
        void onSuccess(String fileId, String webViewLink);
        void onFailure(Exception e);
    }

    public Drive getDriveService() {
        return driveService;
    }

    private static class UploadTask extends AsyncTask<Void, Void, String> {
        private final Drive driveService;
        private final java.io.File localFile;
        private final String mimeType;
        private final DriveUploadCallback callback;
        private Exception error;
        private String webViewLink;

        UploadTask(Drive driveService, java.io.File localFile, String mimeType, DriveUploadCallback callback) {
            this.driveService = driveService;
            this.localFile = localFile;
            this.mimeType = mimeType;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                File fileMetadata = new File();
                fileMetadata.setName(localFile.getName());
                FileContent mediaContent = new FileContent(mimeType, localFile);
                File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, webViewLink")
                        .execute();
                webViewLink = uploadedFile.getWebViewLink();
                return uploadedFile.getId();
            } catch (Exception e) {
                error = e;
                Log.e(TAG, "Drive upload error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String fileId) {
            if (fileId != null) {
                callback.onSuccess(fileId, webViewLink);
            } else {
                callback.onFailure(error);
            }
        }
    }
}
