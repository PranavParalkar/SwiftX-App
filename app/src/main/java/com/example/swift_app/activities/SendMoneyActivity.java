package com.example.swift_app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMoneyActivity extends AppCompatActivity {

    private static final String TAG = "SendMoneyActivity";
    private TextInputEditText etRecipient, etAmount;
    private Button btnSend;
    private ProgressBar progress;
    private SessionManager sessionManager;
    private MaterialCardView cvFeeBreakdown;
    private TextView tvRate, tvFee, tvRecipientGets;
    private double currentRate = 85.80; // Will be updated from API
    private boolean rateLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        sessionManager = new SessionManager(this);

        etRecipient = findViewById(R.id.etRecipient);
        etAmount = findViewById(R.id.etAmount);
        cvFeeBreakdown = findViewById(R.id.cvFeeBreakdown);
        tvRate = findViewById(R.id.tvRate);
        tvFee = findViewById(R.id.tvFee);
        tvRecipientGets = findViewById(R.id.tvRecipientGets);
        btnSend = findViewById(R.id.btnSend);
        progress = findViewById(R.id.sendProgress);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> validateAndSend());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateFees(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Load real-time FX rate
        loadFxRate();
    }

    private void loadFxRate() {
        ApiClient.getFxApi().getLatestRates("USD").enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> rates = (Map<String, Object>) response.body().get("rates");
                        if (rates != null && rates.containsKey("INR")) {
                            Object inrRate = rates.get("INR");
                            if (inrRate instanceof Number) {
                                currentRate = ((Number) inrRate).doubleValue();
                                rateLoaded = true;
                                Log.d(TAG, "Loaded FX rate: 1 USD = " + currentRate + " INR");
                                // Recalculate if amount is already entered
                                if (etAmount.getText() != null && !etAmount.getText().toString().isEmpty()) {
                                    calculateFees(etAmount.getText().toString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing FX rates", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load FX rates, using fallback", t);
                // Keep using fallback rate
            }
        });
    }

    private void calculateFees(String amountStr) {
        if (amountStr.isEmpty()) {
            cvFeeBreakdown.setVisibility(View.GONE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double razorpayFee = amount * 0.02;
            double bankFee = amount * 0.0035;
            double totalFee = razorpayFee + bankFee;
            double recipientGets = amount * currentRate;

            cvFeeBreakdown.setVisibility(View.VISIBLE);
            
            String rateText = String.format(Locale.getDefault(), "1 USD = %.2f INR%s", 
                currentRate, rateLoaded ? " (Live)" : " (Fallback)");
            tvRate.setText(rateText);
            tvFee.setText(String.format(Locale.getDefault(), "$%.2f", totalFee));
            tvRecipientGets.setText(String.format(Locale.getDefault(), "₹%.2f", recipientGets));
        } catch (NumberFormatException e) {
            cvFeeBreakdown.setVisibility(View.GONE);
        }
    }

    private void validateAndSend() {
        if (etRecipient.getText() == null || etAmount.getText() == null) return;

        String identifier = etRecipient.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (identifier.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter recipient and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSend.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        // Lookup recipient by Email or RM ID
        String orFilter = "(rm_id.eq." + identifier + ",email.eq." + identifier + ")";
        ApiClient.getSupabaseApi().getProfileByAny(orFilter).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User recipient = response.body().get(0);
                    if (recipient.getId().equals(sessionManager.getUserId())) {
                        stopLoading("Cannot send money to yourself");
                    } else {
                        executeAtomicTransfer(recipient, amount);
                    }
                } else {
                    stopLoading("Recipient not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                stopLoading("Network error: " + t.getMessage());
            }
        });
    }

    private void executeAtomicTransfer(User recipient, double amount) {
        Map<String, Object> params = prepareTransferParams(recipient, amount);

        ApiClient.getSupabaseApi().executeTransfer(params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SendMoneyActivity.this, "Transfer Successful!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String errorMsg = "Transfer failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("Insufficient balance")) {
                                errorMsg = "Insufficient balance";
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                    }
                    stopLoading(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "RPC execution failed", t);
                stopLoading("Execution failed: " + t.getMessage());
            }
        });
    }

    @NonNull
    private Map<String, Object> prepareTransferParams(User recipient, double amount) {
        String senderId = sessionManager.getUserId();
        double totalFee = (amount * 0.02) + (amount * 0.0035);
        double converted = amount * currentRate;

        Map<String, Object> params = new HashMap<>();
        params.put("p_sender_id", senderId);
        params.put("p_receiver_id", recipient.getId());
        params.put("p_source_currency", "USD");
        params.put("p_target_currency", "INR");
        params.put("p_source_amount", amount);
        params.put("p_target_amount", converted);
        params.put("p_fx_rate", currentRate);
        params.put("p_fee_amount", totalFee);
        params.put("p_note", "Sent from SwiftX Android");
        return params;
    }

    private void stopLoading(String msg) {
        btnSend.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
