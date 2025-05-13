package com.example.my_application;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

public class GoogleDriveSignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "GoogleDriveSignIn";
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No need for a layout, this is a launcher activity for sign-in only

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);
        signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK && data != null) {
                GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener(account -> {
                            Toast.makeText(this, "Signed in as: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                            // You now have a GoogleSignInAccount with Drive access!
                            // You can pass this account to your Drive logic or finish() here.
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Sign-in error", e);
                            finish();
                        });
            } else {
                Toast.makeText(this, "Sign-in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
