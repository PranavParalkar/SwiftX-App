package com.example.swift_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityFragment extends Fragment {

    private RecyclerView rvFullHistory;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        rvFullHistory = view.findViewById(R.id.rvFullHistory);
        rvFullHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionManager = new SessionManager(requireContext());

        fetchHistory();
        return view;
    }

    private void fetchHistory() {
        String userId = sessionManager.getUserId();
        String orFilter = "sender_id.eq." + userId + ",recipient_id.eq." + userId;
        ApiClient.getSupabaseApi().getTransactions(orFilter, "created_at.desc", 100).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(@NonNull Call<List<Transaction>> call, @NonNull Response<List<Transaction>> response) {
                if (!isAdded()) return;
                View v = getView();
                if (v != null && response.isSuccessful() && response.body() != null) {
                    List<Transaction> txns = response.body();
                    View emptyState = v.findViewById(R.id.llEmptyState);
                    if (txns.isEmpty()) {
                        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                        rvFullHistory.setVisibility(View.GONE);
                    } else {
                        if (emptyState != null) emptyState.setVisibility(View.GONE);
                        rvFullHistory.setVisibility(View.VISIBLE);
                        rvFullHistory.setAdapter(new TransactionAdapter(txns, userId));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Transaction>> call, @NonNull Throwable t) {}
        });
    }
}
