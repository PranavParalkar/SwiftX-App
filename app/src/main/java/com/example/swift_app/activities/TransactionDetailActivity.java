package com.example.swift_app.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.Transaction;
import com.google.gson.Gson;

public class TransactionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        String txJson = getIntent().getStringExtra("transaction_json");
        Transaction tx = new Gson().fromJson(txJson, Transaction.class);

        if (tx == null) {
            finish();
            return;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvAmount = findViewById(R.id.tvDetailAmount);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        TextView tvPrevHash = findViewById(R.id.tvPrevHash);
        TextView tvCurrentHash = findViewById(R.id.tvCurrentHash);

        tvAmount.setText(String.format("$%.2f", tx.getAmount()));
        tvStatus.setText(tx.getStatus().toUpperCase());
        tvPrevHash.setText(tx.getPrevHash() != null ? tx.getPrevHash() : "N/A");
        tvCurrentHash.setText(tx.getCurrentHash() != null ? tx.getCurrentHash() : "N/A");

        setupRow(findViewById(R.id.rowSender), "Sender ID", tx.getSenderId());
        setupRow(findViewById(R.id.rowRecipient), "Recipient ID", tx.getRecipientId());
        setupRow(findViewById(R.id.rowDate), "Date", tx.getCreatedAt());
        setupRow(findViewById(R.id.rowType), "Type", tx.getType().toUpperCase());

        // 6. Verify Blockchain Anchoring
        if (tx.getCurrentHash() != null) {
            com.example.swift_app.services.PolygonAnchorService.verifyAnchoring(tx.getCurrentHash(), anchor -> {
                if (anchor != null) {
                    findViewById(R.id.cvPolygonAnchor).setVisibility(android.view.View.VISIBLE);
                    ((TextView) findViewById(R.id.tvPolygonTx)).setText(anchor.getPolygonTxHash());
                    ((TextView) findViewById(R.id.tvPolygonBlock)).setText("Block #" + anchor.getBlockNumber());
                }
            });
        }
    }


    private void setupRow(android.view.View row, String label, String value) {
        ((TextView) row.findViewById(R.id.tvLabel)).setText(label);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value != null ? value : "-");
    }
}
