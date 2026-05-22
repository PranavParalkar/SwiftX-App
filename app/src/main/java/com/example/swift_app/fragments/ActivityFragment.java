package com.example.swift_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        rvFullHistory = view.findViewById(R.id.rvFullHistory);
        rvFullHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionManager = new SessionManager(requireContext());

        fetchHistory();
        return view;
    }

    private void fetchHistory() {
        String userId = sessionManager.getUserId();
        ApiClient.getSupabaseApi().getTransactions("eq." + userId).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rvFullHistory.setAdapter(new TransactionAdapter(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {}
        });
    }
}
