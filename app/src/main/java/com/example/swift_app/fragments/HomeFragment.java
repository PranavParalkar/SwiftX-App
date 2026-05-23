package com.example.swift_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.adapters.TransactionAdapter;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvTotalBalance, tvAvatarInitial, tvAiInsight;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;
    private com.example.swift_app.services.AiService aiService;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        tvUserName = view.findViewById(R.id.tvUserName);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvAvatarInitial = view.findViewById(R.id.tvAvatarInitial);
        tvAiInsight = view.findViewById(R.id.tvAiInsightDescription);
        rvTransactions = view.findViewById(R.id.rvRecentTransactions);


        aiService = new com.example.swift_app.services.AiService();


        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), sessionManager.getUserId());
        rvTransactions.setAdapter(adapter);

        updateUI();
        fetchBalance();
        fetchTransactions();

        view.findViewById(R.id.btnSendMoney).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.example.swift_app.activities.SendMoneyActivity.class));
        });

        view.findViewById(R.id.btnReceive).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.example.swift_app.activities.ReceiveMoneyActivity.class));
        });

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.example.swift_app.activities.AddFundsActivity.class));
        });

        view.findViewById(R.id.btnExchange).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.example.swift_app.activities.ExchangeActivity.class));
        });

        view.findViewById(R.id.cvInsight).setOnClickListener(v -> {

            if (getActivity() instanceof com.example.swift_app.activities.MainActivity) {
                ((com.example.swift_app.activities.MainActivity) getActivity()).loadInsightsFragment();
            }
        });



        return view;
    }

    private void updateUI() {
        String name = sessionManager.getUserName();
        tvUserName.setText(name);
        tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        aiService.getPersonalizedInsight(sessionManager.getUserId(), insight -> {
            if (isAdded()) tvAiInsight.setText(insight);
        });
    }

    private void fetchBalance() {
        String userId = sessionManager.getUserId();
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<com.example.swift_app.models.Wallet>>() {
            @Override
            public void onResponse(Call<List<com.example.swift_app.models.Wallet>> call, Response<List<com.example.swift_app.models.Wallet>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    com.example.swift_app.models.Wallet wallet = response.body().get(0);
                    tvTotalBalance.setText(String.format("$%.2f", wallet.getBalance()));
                }
            }

            @Override
            public void onFailure(Call<List<com.example.swift_app.models.Wallet>> call, Throwable t) {}
        });
    }


    private void fetchTransactions() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        String orFilter = "sender_id.eq." + userId + ",recipient_id.eq." + userId;
        ApiClient.getSupabaseApi().getTransactions(orFilter, "created_at.desc", 5).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    adapter.updateList(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                // handle error
            }
        });
    }
}
