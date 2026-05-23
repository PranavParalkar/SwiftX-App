package com.example.swift_app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFundsActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private Button btnAdd;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_funds);

        sessionManager = new SessionManager(this);
        etAmount = findViewById(R.id.etAmount);
        btnAdd = findViewById(R.id.btnAddFunds);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) return;

            double amount = Double.parseDouble(amountStr);
            processDeposit(amount);
        });
    }

    private void processDeposit(double amount) {
        String userId = sessionManager.getUserId();
        
        // 1. Get user's primary wallet
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wallet wallet = response.body().get(0);
                    updateBalance(wallet, amount);
                } else {
                    Toast.makeText(AddFundsActivity.this, "No wallet found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(AddFundsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBalance(Wallet wallet, double depositAmount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("balance", wallet.getBalance() + depositAmount);

        ApiClient.getSupabaseApi().updateWallet("eq." + wallet.getId(), updates).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddFundsActivity.this, "Successfully added funds!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddFundsActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(AddFundsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
