package com.example.swift_app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExchangeActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private TextView tvConversionResult;
    private Button btnExchange;
    private SessionManager sessionManager;
    private double currentRate = 0.92; // Mock USD to EUR rate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);

        sessionManager = new SessionManager(this);
        etAmount = findViewById(R.id.etAmount);
        tvConversionResult = findViewById(R.id.tvConversionResult);
        btnExchange = findViewById(R.id.btnExchange);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConversion(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnExchange.setOnClickListener(v -> performExchange());
    }

    private void updateConversion(String amountStr) {
        if (amountStr.isEmpty()) {
            tvConversionResult.setText("≈ 0.00 EUR");
            return;
        }
        double amount = Double.parseDouble(amountStr);
        double result = amount * currentRate;
        tvConversionResult.setText(String.format(Locale.getDefault(), "≈ %.2f EUR", result));
    }

    private void performExchange() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) return;

        double amount = Double.parseDouble(amountStr);
        String userId = sessionManager.getUserId();

        // Mock exchange logic: Reduce USD wallet, Increase EUR wallet (if exists)
        // For simplicity, we just show a success message for now
        Toast.makeText(this, "Currency Exchanged Successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
