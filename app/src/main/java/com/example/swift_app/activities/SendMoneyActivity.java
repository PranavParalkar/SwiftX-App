package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.services.AmlRulesEngine;
import com.example.swift_app.services.HashChainService;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMoneyActivity extends AppCompatActivity {

    private TextInputEditText etRecipient, etAmount;
    private TextView tvConvertedAmount;
    private Button btnSend;
    private ProgressBar progress;
    private SessionManager sessionManager;
    private final String targetCurrency = "EUR"; // Default for demo

    private final ActivityResultLauncher<Intent> contactsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String email = result.getData().getStringExtra("selected_email");
                    if (etRecipient != null) {
                        etRecipient.setText(email);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        sessionManager = new SessionManager(this);

        etRecipient = findViewById(R.id.etRecipientEmail);
        etAmount = findViewById(R.id.etAmount);
        tvConvertedAmount = findViewById(R.id.tvConvertedAmount);
        btnSend = findViewById(R.id.btnSend);
        progress = findViewById(R.id.sendProgress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> validateAndSend());

        com.google.android.material.textfield.TextInputLayout tilRecipient = findViewById(R.id.tilRecipient);
        if (tilRecipient != null) {
            tilRecipient.setEndIconOnClickListener(v -> {
                Intent intent = new Intent(this, ContactsActivity.class);
                contactsLauncher.launch(intent);
            });
        }

        if (etAmount != null) {
            etAmount.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateFxConversion(s.toString());
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void updateFxConversion(String amountStr) {
        if (amountStr.isEmpty()) {
            if (tvConvertedAmount != null) tvConvertedAmount.setVisibility(View.GONE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            ApiClient.getFxApi().convert("USD", targetCurrency, amount).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Object result = response.body().get("result");
                        if (result != null && tvConvertedAmount != null) {
                            tvConvertedAmount.setVisibility(View.VISIBLE);
                            tvConvertedAmount.setText(String.format(Locale.getDefault(), "≈ %.2f %s", Double.parseDouble(result.toString()), targetCurrency));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                }
            });
        } catch (NumberFormatException e) {
            if (tvConvertedAmount != null) tvConvertedAmount.setVisibility(View.GONE);
        }
    }

    private void validateAndSend() {
        if (etRecipient == null || etAmount == null) return;
        
        String recipientEmail = etRecipient.getText() != null ? etRecipient.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

        if (recipientEmail.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        final double amountVal;
        try {
            amountVal = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        String userId = sessionManager.getUserId();
        String orFilter = "sender_id.eq." + userId + ",recipient_id.eq." + userId;
        ApiClient.getSupabaseApi().getTransactions(orFilter, "created_at.desc", 20)
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> history = response.body();
                    AmlRulesEngine.AmlResult amlResult = AmlRulesEngine.checkTransfer(amountVal, "USD", history, userId);
                    if (!AmlRulesEngine.canProceed(amlResult)) {
                        stopLoading("Blocked by AML: " + (amlResult.getTriggeredRules().isEmpty() ? "Risk factor" : amlResult.getTriggeredRules().get(0)));
                        return;
                    }
                    findRecipient(recipientEmail, amountVal, history);
                } else {
                    stopLoading("Sync error. Try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {
                stopLoading("Network error");
            }
        });
    }

    private void findRecipient(String email, double amount, List<Transaction> history) {
        ApiClient.getSupabaseApi().getProfile("eq." + email).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    executeTransfer(response.body().get(0), amount, history);
                } else {
                    stopLoading("Recipient not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                stopLoading("Network error");
            }
        });
    }

    private void executeTransfer(User recipient, double amount, List<Transaction> history) {
        final String senderId = sessionManager.getUserId();
        
        Transaction tx = new Transaction();
        tx.setSenderId(senderId);
        tx.setRecipientId(recipient.getId());
        tx.setSenderName(sessionManager.getUserName());
        tx.setRecipientName(recipient.getFullName());
        tx.setAmount(amount);
        tx.setCurrency("USD");
        tx.setType("transfer");
        tx.setStatus("completed");

        String prevHash = HashChainService.getChainRoot(history);
        String currentHash = HashChainService.computeHash(tx, prevHash);
        tx.setPrevHash(prevHash);
        tx.setCurrentHash(currentHash);

        ApiClient.getSupabaseApi().createTransaction(tx).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    updateWallets(senderId, recipient.getId(), amount);
                } else {
                    stopLoading("Transfer failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {
                stopLoading("Persistence error");
            }
        });
    }

    private void updateWallets(@SuppressWarnings("unused") String senderId, 
                               @SuppressWarnings("unused") String recipientId, 
                               @SuppressWarnings("unused") double amount) {
        Toast.makeText(this, "Transfer Successful!", Toast.LENGTH_LONG).show();
        finish();
    }


    private void stopLoading(String msg) {
        btnSend.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
