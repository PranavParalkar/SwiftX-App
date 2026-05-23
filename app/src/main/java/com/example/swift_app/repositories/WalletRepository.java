package com.example.swift_app.repositories;

import androidx.annotation.NonNull;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.services.HashChainService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRepository {

    public interface WalletCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void depositFunds(String userId, double amount, WalletCallback<Transaction> callback) {
        // 1. Get user's primary wallet
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wallet wallet = response.body().get(0);
                    getLatestTransactionAndProceed(wallet, amount, callback);
                } else {
                    callback.onError("No wallet found for building chain");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void getLatestTransactionAndProceed(Wallet wallet, double amount, WalletCallback<Transaction> callback) {
        // 2. Get latest transaction to compute next hash
        ApiClient.getSupabaseApi().getTransactions("sender_id.eq." + wallet.getUserId() + ",recipient_id.eq." + wallet.getUserId(), "created_at.desc", 1)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                        String prevHash = HashChainService.getGenesisHash();
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            prevHash = response.body().get(0).getCurrentHash();
                        }
                        
                        executeDeposit(wallet, amount, prevHash, callback);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {
                        // Even if fails, we might start a new chain with genesis
                        executeDeposit(wallet, amount, HashChainService.getGenesisHash(), callback);
                    }
                });
    }

    private void executeDeposit(Wallet wallet, double amount, String prevHash, WalletCallback<Transaction> callback) {
        // 3. Create Transaction Record
        Transaction tx = new Transaction();
        tx.setRecipientId(wallet.getUserId()); // Depositing to self
        tx.setAmount(amount);
        tx.setCurrency(wallet.getCurrency());
        tx.setType("deposit");
        tx.setPrevHash(prevHash);
        tx.setStatus("completed");
        
        // Compute Hash
        String currentHash = HashChainService.computeHash(tx, prevHash);
        tx.setCurrentHash(currentHash);

        ApiClient.getSupabaseApi().createTransaction(tx).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (response.isSuccessful()) {
                    // 4. Update Balance
                    updateBalance(wallet, amount, tx, callback);
                } else {
                    callback.onError("Failed to record transaction ledger");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void updateBalance(Wallet wallet, double amount, Transaction tx, WalletCallback<Transaction> callback) {
        Map<String, Object> updates = new HashMap<>();
        // Defaulting to usd_balance for repository deposits
        updates.put("usd_balance", wallet.getUsdBalance() + amount);

        ApiClient.getSupabaseApi().updateWallet("eq." + wallet.getId(), updates).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(tx);
                } else {
                    callback.onError("Ledger updated but balance update failed. Please contact support.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
