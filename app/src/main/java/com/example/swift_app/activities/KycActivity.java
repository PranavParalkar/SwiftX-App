package com.example.swift_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private Button btnSubmit;
    private Spinner spinner;

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
        String[] types = {"Passport", "National ID", "Driver's License"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinner.setAdapter(adapter);

        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            Toast.makeText(this, "Camera/Gallery module started", Toast.LENGTH_SHORT).show();
        });

        btnSubmit = findViewById(R.id.btnSubmitKyc);
        btnSubmit.setOnClickListener(v -> submitKyc());
    }

    private void submitKyc() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Sending to Admin...");

        // Simulate backend redirection to Admin queue by updating profile status
        Map<String, Object> updates = new HashMap<>();
        updates.put("kyc_status", "pending_review");

        ApiClient.getSupabaseApi().updateProfile("eq." + userId, updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(KycActivity.this, 
                        "KYC successfully routed to Admin Review Queue.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit KYC");
                    Toast.makeText(KycActivity.this, "Submission failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit KYC");
                Toast.makeText(KycActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
