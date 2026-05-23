package com.example.swift_app.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.example.swift_app.activities.AdminDashboardActivity;
import com.example.swift_app.activities.ExchangeActivity;
import com.example.swift_app.activities.MainActivity;
import com.example.swift_app.activities.ReceiveMoneyActivity;
import com.example.swift_app.activities.SendMoneyActivity;
import com.example.swift_app.adapters.TransactionAdapter;
import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.Wallet;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.services.AiService;
import com.example.swift_app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvUsdBalance, tvInrBalance, tvVaultBalance, tvAvatarInitial, tvAiInsight, tvRmId;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private SessionManager sessionManager;
    private AiService aiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sessionManager = new SessionManager(requireContext());
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUsdBalance = view.findViewById(R.id.tvUsdBalance);
        tvInrBalance = view.findViewById(R.id.tvInrBalance);
        tvVaultBalance = view.findViewById(R.id.tvVaultBalance);
        tvRmId = view.findViewById(R.id.tvRmId);
        tvAvatarInitial = view.findViewById(R.id.tvAvatarInitial);
        tvAiInsight = view.findViewById(R.id.tvAiInsightDescription);
        rvTransactions = view.findViewById(R.id.rvRecentTransactions);

        aiService = new AiService();

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), sessionManager.getUserId());
        rvTransactions.setAdapter(adapter);

        updateUI();
        fetchProfile();
        fetchBalance();
        fetchTransactions();

        view.findViewById(R.id.btnCopyRmId).setOnClickListener(v -> {
            String mid = tvRmId.getText() != null ? tvRmId.getText().toString() : "";
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("SwiftX ID", mid);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "ID Copied to Clipboard", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnSendMoney).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), SendMoneyActivity.class)));

        view.findViewById(R.id.btnReceive).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ReceiveMoneyActivity.class)));

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), AddFundsActivity.class)));

        view.findViewById(R.id.btnExchange).setOnClickListener(v -> 
            startActivity(new Intent(getActivity(), ExchangeActivity.class)));

        view.findViewById(R.id.cvInsight).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadInsightsFragment();
            }
        });

        // Admin Panel Logic
        View cvAdminPanel = view.findViewById(R.id.cvAdminPanel);
        String userEmail = sessionManager.getUserEmail();
        
        if ("admin@swiftx.ai".equalsIgnoreCase(userEmail)) {
            cvAdminPanel.setVisibility(View.VISIBLE);
            cvAdminPanel.setOnClickListener(v -> 
                startActivity(new Intent(getActivity(), AdminDashboardActivity.class)));
        }

        return view;
    }

    private void fetchProfile() {
        String userId = sessionManager.getUserId();
        ApiClient.getSupabaseApi().getProfile("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    if (user.getRmId() != null) tvRmId.setText(user.getRmId());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                // Log error
            }
        });
    }

    private void updateUI() {
        String name = sessionManager.getUserName();
        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
            tvAvatarInitial.setText(String.valueOf(name.charAt(0)).toUpperCase(Locale.getDefault()));
        }
        aiService.getPersonalizedInsight(sessionManager.getUserId(), insight -> {
            if (isAdded()) tvAiInsight.setText(insight);
        });
    }

    private void fetchBalance() {
        String userId = sessionManager.getUserId();
        ApiClient.getSupabaseApi().getWallets("eq." + userId).enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Wallet>> call, @NonNull Response<List<Wallet>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Wallet wallet = response.body().get(0);
                    tvUsdBalance.setText(String.format(Locale.getDefault(), "$%.2f", wallet.getUsdBalance()));
                    tvInrBalance.setText(String.format(Locale.getDefault(), "₹%.2f", wallet.getInrBalance()));
                    tvVaultBalance.setText(String.format(Locale.getDefault(), "$%.2f", wallet.getSavingsBalance()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Wallet>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchTransactions() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        String orFilter = "(sender_id.eq." + userId + ",recipient_id.eq." + userId + ")";
        ApiClient.getSupabaseApi().getTransactions(orFilter, "created_at.desc", 5).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    adapter.updateList(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {}
        });
    }
}
