package com.example.swift_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.User;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.services.AmlRulesEngine;
import com.example.swift_app.services.HashChainService;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMoneyActivity extends AppCompatActivity {

    private TextInputEditText etRecipient, etAmount;
    private Button btnSend;
    private ProgressBar progress;
    private SessionManager sessionManager;
    private AmlRulesEngine amlEngine;
    private HashChainService hashChainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        sessionManager = new SessionManager(this);
        amlEngine = new AmlRulesEngine();
        hashChainService = new HashChainService(this);

        etRecipient = findViewById(R.id.etRecipientEmail);
        etAmount = findViewById(R.id.etAmount);
        btnSend = findViewById(R.id.btnSend);
        progress = findViewById(R.id.sendProgress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> validateAndSend());
    }

    private void validateAndSend() {
        String recipientEmail = etRecipient.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (recipientEmail.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // 1. AML Engine Check
        if (!amlEngine.isTransactionAllowed(amount, "USD")) {
            Toast.makeText(this, "Transaction blocked by AML: Limit exceeded", Toast.LENGTH_LONG).show();
            return;
        }

        btnSend.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        // 2. Find Recipient
        ApiClient.getSupabaseApi().getProfile("eq." + recipientEmail).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    executeTransfer(response.body().get(0), amount);
                } else {
                    stopLoading("Recipient not found");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                stopLoading("Network error");
            }
        });
    }

    private void executeTransfer(User recipient, double amount) {
        String senderId = sessionManager.getUserId();
        
        // 3. Create Transaction Object
        Transaction tx = new Transaction();
        tx.setSenderId(senderId);
        tx.setRecipientId(recipient.getId());
        tx.setSenderName(sessionManager.getUserName());
        tx.setRecipientName(recipient.getFullName());
        tx.setAmount(amount);
        tx.setCurrency("USD");
        tx.setType("transfer");
        tx.setStatus("completed");

        // 4. Cryptographic Proof (Integrity Hash)
        String prevHash = hashChainService.getLastKnownHash();
        String currentHash = hashChainService.generateTransactionHash(tx, prevHash);
        tx.setPrevHash(prevHash);
        tx.setCurrentHash(currentHash);

        // 5. Save to Supabase
        ApiClient.getSupabaseApi().createTransaction(tx).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    hashChainService.saveLastHash(currentHash);
                    updateWallets(senderId, recipient.getId(), amount);
                } else {
                    stopLoading("Transfer failed");
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                stopLoading("Persistence error");
            }
        });
    }

    private void updateWallets(String senderId, String recipientId, double amount) {
        // In a real app, this should be a DB function (RPC) for atomicity
        // For this demo, we assume success after the transaction record is created
        Toast.makeText(this, "Transfer Successful!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void stopLoading(String msg) {
        btnSend.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
