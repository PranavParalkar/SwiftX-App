package com.example.swift_app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;

public class KycActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Spinner spinner = findViewById(R.id.spnDocType);
        String[] types = {"Passport", "National ID", "Driver's License"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinner.setAdapter(adapter);

        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            Toast.makeText(this, "Camera opened (Mock)", Toast.LENGTH_SHORT).show();
        });

        Button btnSubmit = findViewById(R.id.btnSubmitKyc);
        btnSubmit.setOnClickListener(v -> {
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Submitting...");
            
            v.postDelayed(() -> {
                Toast.makeText(KycActivity.this, "KYC Submitted. We will review it shortly.", Toast.LENGTH_LONG).show();
                finish();
            }, 2000);
        });
    }
}
