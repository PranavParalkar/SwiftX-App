package com.example.swift_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.swift_app.R;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends Fragment {

    private TextView tvWalletBalance;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);
        sessionManager = new SessionManager(requireContext());
        
        fetchWalletData();
        return view;
    }

    private void fetchWalletData() {
        String userId = sessionManager.getUserId();
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wallet wallet = response.body().get(0);
                    tvWalletBalance.setText(String.format("$%.2f", wallet.getBalance()));
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });
    }
}
