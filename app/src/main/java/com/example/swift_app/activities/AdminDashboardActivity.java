package com.example.swift_app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.adapters.KycRequestAdapter;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvKycRequests;
    private KycRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvKycRequests = findViewById(R.id.rvKycRequests);
        rvKycRequests.setLayoutManager(new LinearLayoutManager(this));

        adapter = new KycRequestAdapter(new ArrayList<>(), new KycRequestAdapter.OnKycActionListener() {
            @Override
            public void onApprove(User user) {
                updateKycStatus(user, "verified");
            }

            @Override
            public void onReject(User user) {
                updateKycStatus(user, "rejected");
            }
        });

        rvKycRequests.setAdapter(adapter);
        fetchPendingKyc();
    }

    private void fetchPendingKyc() {
        ApiClient.getSupabaseApi().getProfiles().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> pending = new ArrayList<>();
                    for (User u : response.body()) {
                        if ("pending_review".equals(u.getKycStatus())) {
                            pending.add(u);
                        }
                    }
                    adapter.updateList(pending);
                    if (pending.isEmpty()) {
                        Toast.makeText(AdminDashboardActivity.this, "No pending requests", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateKycStatus(User user, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("kyc_status", newStatus);

        ApiClient.getSupabaseApi().updateProfile("eq." + user.getId(), updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminDashboardActivity.this, "User " + newStatus, Toast.LENGTH_SHORT).show();
                    fetchPendingKyc(); // Refresh list
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Error updating status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
