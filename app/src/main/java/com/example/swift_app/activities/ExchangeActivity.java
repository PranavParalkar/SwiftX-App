package com.example.swift_app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class ExchangeActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private TextView tvConversionResult;
    private final double currentRate = 0.92; // Mock USD to EUR rate - marked final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);

        etAmount = findViewById(R.id.etAmount);
        tvConversionResult = findViewById(R.id.tvConversionResult);
        Button btnExchange = findViewById(R.id.btnExchange); // Local variable

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
    }

    private void updateConversion(String amountStr) {
        if (amountStr.isEmpty()) {
            tvConversionResult.setText(R.string.placeholder_eur_zero);
            return;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            double result = amount * currentRate;
            tvConversionResult.setText(String.format(Locale.getDefault(), "≈ %.2f EUR", result));
        } catch (NumberFormatException e) {
            tvConversionResult.setText(R.string.placeholder_eur_zero);
        }
    }

    private void performExchange() {
        if (etAmount.getText() == null) return;
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) return;

        // Mock exchange logic
        Toast.makeText(this, "Currency Exchanged Successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
