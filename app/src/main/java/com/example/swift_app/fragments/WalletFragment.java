package com.example.swift_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.activities.AddFundsActivity;
import com.example.swift_app.adapters.WalletAdapter;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.CurrencyFormatter;
import com.example.swift_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends Fragment {

    private TextView tvWalletBalance, tvCurrencyCount;
    private RecyclerView rvWallets;
    private WalletAdapter walletAdapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        sessionManager = new SessionManager(requireContext());
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        tvCurrencyCount = view.findViewById(R.id.tvCurrencyCount);
        rvWallets = view.findViewById(R.id.rvWallets);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext()));
        walletAdapter = new WalletAdapter(new ArrayList<>());
        rvWallets.setAdapter(walletAdapter);

        // Quick action: Deposit
        view.findViewById(R.id.btnDeposit).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddFundsActivity.class));
        });

        // Quick action: Withdraw (placeholder for now)
        view.findViewById(R.id.btnWithdraw).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Withdraw feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Add new currency
        view.findViewById(R.id.btnAddCurrency).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Multi-currency setup coming soon", Toast.LENGTH_SHORT).show();
        });

        fetchWalletData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchWalletData(); // Refresh balance when returning from deposit
    }

    private void fetchWalletData() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<Wallet> wallets = response.body();
                    walletAdapter.updateList(wallets);

                    // Update total balance (sum of all wallets)
                    double totalBalance = 0;
                    String primaryCurrency = "USD";
                    for (Wallet w : wallets) {
                        totalBalance += w.getBalance();
                        if (wallets.indexOf(w) == 0) {
                            primaryCurrency = w.getCurrency();
                        }
                    }
                    tvWalletBalance.setText(CurrencyFormatter.format(totalBalance, primaryCurrency));
                    tvCurrencyCount.setText(String.format(Locale.getDefault(), "%d account%s", wallets.size(), wallets.size() != 1 ? "s" : ""));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load wallets", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
