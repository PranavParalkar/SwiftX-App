package com.example.swift_app.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.repositories.WalletRepository;
import com.example.swift_app.services.AmlRulesEngine;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddFundsActivity extends AppCompatActivity {

    private TextInputEditText etAmount;
    private MaterialButton btnAdd;
    private ChipGroup chipGroup;
    private SessionManager sessionManager;
    private WalletRepository walletRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_funds);

        sessionManager = new SessionManager(this);
        walletRepository = new WalletRepository();

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        btnAdd = findViewById(R.id.btnAddFunds);
        chipGroup = findViewById(R.id.chipGroupAmounts);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // Quick amounts
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = findViewById(checkedIds.get(0));
                String text = chip.getText().toString().replace("+$", "");
                etAmount.setText(text);
            }
        });

        // Amount validation
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasAmount = s.length() > 0;
                btnAdd.setEnabled(hasAmount);
                if (hasAmount) {
                    btnAdd.setAlpha(1.0f);
                } else {
                    btnAdd.setAlpha(0.5f);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAdd.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) return;

            try {
                double amount = Double.parseDouble(amountStr);
                validateAndProcess(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndProcess(double amount) {
        // 1. AML Check
        AmlRulesEngine.AmlResult amlResult = AmlRulesEngine.checkDeposit(amount, "USD", sessionManager.getUserId());
        
        if (!AmlRulesEngine.canProceed(amlResult)) {
            showAmlBlockDialog(amlResult);
            return;
        }

        if (amlResult.isFlagged()) {
            showAmlWarningAndProceed(amount, amlResult);
        } else {
            executeDeposit(amount);
        }
    }

    private void executeDeposit(double amount) {
        btnAdd.setEnabled(false);
        btnAdd.setText("Processing Ledger...");

        walletRepository.depositFunds(sessionManager.getUserId(), amount, new WalletRepository.WalletCallback<Transaction>() {
            @Override
            public void onSuccess(Transaction result) {
                showSuccessDialog(result);
            }

            @Override
            public void onError(String message) {
                btnAdd.setEnabled(true);
                btnAdd.setText("Confirm Deposit");
                Toast.makeText(AddFundsActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAmlBlockDialog(AmlRulesEngine.AmlResult result) {
        new AlertDialog.Builder(this)
                .setTitle("Limit Reached")
                .setMessage("This deposit exceeds your current compliance limits. Please complete your KYC verification to increase limits.\n\nTriggered: " + result.getTriggeredRules().get(0))
                .setPositiveButton("Verify Identity", (d, w) -> {
                    // Navigate to KYC
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAmlWarningAndProceed(double amount, AmlRulesEngine.AmlResult result) {
        new AlertDialog.Builder(this)
                .setTitle("Compliance Review")
                .setMessage("Large deposits are subject to standard compliance checks. This may take up to 24 hours to clear in some regions.\n\nDo you wish to proceed?")
                .setPositiveButton("Yes, Proceed", (d, w) -> executeDeposit(amount))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSuccessDialog(Transaction tx) {
        View view = getLayoutInflater().inflate(R.layout.dialog_success_deposit, null);
        
        TextView hashView = view.findViewById(R.id.tvHash);
        if (hashView != null && tx.getCurrentHash() != null) {
            hashView.setText(tx.getCurrentHash());
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Back to Wallet", (d, w) -> finish())
                .show();
    }
}
