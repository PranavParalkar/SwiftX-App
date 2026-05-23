package com.example.swift_app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.Constants;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExchangeActivity extends AppCompatActivity {

    private static final String TAG = "ExchangeActivity";
    private TextInputEditText etAmount;
    private AutoCompleteTextView actvFromCurrency, actvToCurrency;
    private TextView tvConversionResult, tvExchangeRate;
    private Button btnExchange;
    private ProgressBar progress;
    private SessionManager sessionManager;
    private Map<String, Double> currentRates = new HashMap<>();
    private String fromCurrency = "USD";
    private String toCurrency = "EUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);

        sessionManager = new SessionManager(this);
        
        etAmount = findViewById(R.id.etAmount);
        tvConversionResult = findViewById(R.id.tvConversionResult);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        btnExchange = findViewById(R.id.btnExchange);
        progress = findViewById(R.id.exchangeProgress);
        
        // Setup currency dropdowns if they exist in layout
        actvFromCurrency = findViewById(R.id.actvFromCurrency);
        actvToCurrency = findViewById(R.id.actvToCurrency);
        
        if (actvFromCurrency != null && actvToCurrency != null) {
            setupCurrencyDropdowns();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConversion(s != null ? s.toString() : "");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnExchange.setOnClickListener(v -> performExchange());
        
        // Load exchange rates
        loadExchangeRates();
    }

    private void setupCurrencyDropdowns() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.CURRENCIES);
        actvFromCurrency.setAdapter(adapter);
        actvToCurrency.setAdapter(adapter);
        
        actvFromCurrency.setText("USD", false);
        actvToCurrency.setText("EUR", false);
        
        actvFromCurrency.setOnItemClickListener((parent, view, position, id) -> {
            fromCurrency = Constants.CURRENCIES[position];
            loadExchangeRates();
        });
        
        actvToCurrency.setOnItemClickListener((parent, view, position, id) -> {
            toCurrency = Constants.CURRENCIES[position];
            updateConversion(etAmount.getText() != null ? etAmount.getText().toString() : "");
        });
    }

    private void loadExchangeRates() {
        ApiClient.getFxApi().getLatestRates(fromCurrency).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> rates = (Map<String, Object>) response.body().get("rates");
                        if (rates != null) {
                            currentRates.clear();
                            for (Map.Entry<String, Object> entry : rates.entrySet()) {
                                if (entry.getValue() instanceof Number) {
                                    currentRates.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
                                }
                            }
                            Log.d(TAG, "Loaded " + currentRates.size() + " exchange rates");
                            updateConversion(etAmount.getText() != null ? etAmount.getText().toString() : "");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing exchange rates", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load exchange rates", t);
                Toast.makeText(ExchangeActivity.this, "Failed to load rates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateConversion(String amountStr) {
        if (amountStr.isEmpty() || currentRates.isEmpty()) {
            tvConversionResult.setText("0.00 " + toCurrency);
            if (tvExchangeRate != null) {
                tvExchangeRate.setText("Loading rates...");
            }
            return;
        }
        
        try {
            double amount = Double.parseDouble(amountStr);
            Double rate = currentRates.get(toCurrency);
            
            if (rate != null) {
                double result = amount * rate;
                tvConversionResult.setText(String.format(Locale.getDefault(), "≈ %.2f %s", result, toCurrency));
                if (tvExchangeRate != null) {
                    tvExchangeRate.setText(String.format(Locale.getDefault(), 
                        "1 %s = %.4f %s", fromCurrency, rate, toCurrency));
                }
            } else {
                tvConversionResult.setText("Rate unavailable");
            }
        } catch (NumberFormatException e) {
            tvConversionResult.setText("0.00 " + toCurrency);
        }
    }

    private void performExchange() {
        if (etAmount.getText() == null) return;
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
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

        Double rate = currentRates.get(toCurrency);
        if (rate == null) {
            Toast.makeText(this, "Exchange rate not available", Toast.LENGTH_SHORT).show();
            return;
        }

        btnExchange.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        // In a real implementation, this would update wallet balances
        // For now, we'll simulate the exchange
        new android.os.Handler().postDelayed(() -> {
            btnExchange.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            
            double result = amount * rate;
            String message = String.format(Locale.getDefault(), 
                "Exchanged %.2f %s to %.2f %s", amount, fromCurrency, result, toCurrency);
            Toast.makeText(ExchangeActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        }, 1500);
    }
}
