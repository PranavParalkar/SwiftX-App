package com.example.swift_app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.swift_app.R;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KycActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private Button btnSubmit, btnUpload;
    private Spinner spinner;
    private ImageView ivDocumentPreview;
    private Uri selectedDocumentUri;
    
    private final ActivityResultLauncher<Intent> documentPickerLauncher = 
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedDocumentUri = result.getData().getData();
                if (selectedDocumentUri != null) {
                    if (ivDocumentPreview != null) {
                        ivDocumentPreview.setImageURI(selectedDocumentUri);
                        ivDocumentPreview.setVisibility(android.view.View.VISIBLE);
                    }
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Document selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    
    private final ActivityResultLauncher<String> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openDocumentPicker();
            } else {
                Toast.makeText(this, "Permission required to select documents", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);

        sessionManager = new SessionManager(this);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        spinner = findViewById(R.id.spnDocType);
        String[] types = {"Passport", "National ID", "Driver's License", "Residence Permit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinner.setAdapter(adapter);

        // ivDocumentPreview = findViewById(R.id.ivDocumentPreview); // Optional - may not be in layout
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> checkPermissionAndPickDocument());

        btnSubmit = findViewById(R.id.btnSubmitKyc);
        btnSubmit.setEnabled(false);
        btnSubmit.setOnClickListener(v -> submitKyc());
    }

    private void checkPermissionAndPickDocument() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES
        // For older versions, we need READ_EXTERNAL_STORAGE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                openDocumentPicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                openDocumentPicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        documentPickerLauncher.launch(intent);
    }

    private void submitKyc() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDocumentUri == null) {
            Toast.makeText(this, "Please upload a document first", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // In a production app, you would:
        // 1. Upload the document to Supabase Storage
        // 2. Get the storage URL
        // 3. Update the profile with the document URL and status
        
        // For now, we'll just update the KYC status to submitted
        Map<String, Object> updates = new HashMap<>();
        updates.put("kyc_status", "submitted");

        ApiClient.getSupabaseApi().updateProfile("eq." + userId, updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(KycActivity.this, 
                        "KYC submitted successfully! Admin will review within 24-48 hours.", 
                        Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit KYC");
                    Toast.makeText(KycActivity.this, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit KYC");
                Toast.makeText(KycActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
