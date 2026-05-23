package com.example.swift_app.repositories;

import androidx.annotation.NonNull;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRepository {

    public interface WalletCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void depositFunds(String userId, double amount, WalletCallback<Transaction> callback) {
        // 1. Get user's unified wallet
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wallet wallet = response.body().get(0);
                    executeDeposit(wallet, amount, callback);
                } else {
                    callback.onError("No wallet found. Please contact support.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void executeDeposit(Wallet wallet, double amount, WalletCallback<Transaction> callback) {
        // Create a deposit transaction record
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setSenderId(wallet.getUserId()); // Self-deposit
        tx.setReceiverId(wallet.getUserId());
        tx.setSourceCurrency("USD");
        tx.setTargetCurrency("USD");
        tx.setSourceAmount(amount);
        tx.setTargetAmount(amount);
        tx.setFxRate(1.0);
        tx.setFeeAmount(0.0);
        tx.setStatus("completed");
        tx.setNote("Deposit via Android App");

        // Create transaction record
        ApiClient.getSupabaseApi().createTransaction(tx).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Transaction createdTx = response.body().get(0);
                    // Update wallet balance
                    updateBalance(wallet, amount, createdTx, callback);
                } else {
                    callback.onError("Failed to record transaction");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {
                callback.onError("Transaction failed: " + t.getMessage());
            }
        });
    }

    private void updateBalance(Wallet wallet, double amount, Transaction tx, WalletCallback<Transaction> callback) {
        Map<String, Object> updates = new HashMap<>();
        // Update USD balance in unified wallet
        updates.put("usd_balance", wallet.getUsdBalance() + amount);

        ApiClient.getSupabaseApi().updateWallet("eq." + wallet.getId(), updates).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(tx);
                } else {
                    callback.onError("Transaction recorded but balance update failed. Please refresh.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {
                callback.onError("Balance update failed: " + t.getMessage());
            }
        });
    }
}
