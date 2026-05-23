package com.example.swift_app.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.adapters.AuditLogAdapter;
import com.example.swift_app.adapters.KycRequestAdapter;
import com.example.swift_app.models.AuditLog;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private KycRequestAdapter kycAdapter;
    private AuditLogAdapter auditAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        RecyclerView rvRequests = findViewById(R.id.rvRequests);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        kycAdapter = new KycRequestAdapter(new ArrayList<>(), new KycRequestAdapter.OnKycActionListener() {
            @Override
            public void onApprove(User user) {
                updateKycStatus(user, "verified");
            }

            @Override
            public void onReject(User user) {
                updateKycStatus(user, "rejected");
            }
        });

        auditAdapter = new AuditLogAdapter(new ArrayList<>());
        rvRequests.setAdapter(kycAdapter);
        fetchPendingKyc();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvRequests.setAdapter(kycAdapter);
                    fetchPendingKyc();
                } else {
                    rvRequests.setAdapter(auditAdapter);
                    fetchAuditLogs();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchPendingKyc() {
        ApiClient.getSupabaseApi().getProfiles().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> pending = new ArrayList<>();
                    for (User u : response.body()) {
                        if ("pending_review".equals(u.getKycStatus())) {
                            pending.add(u);
                        }
                    }
                    kycAdapter.updateList(pending);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchAuditLogs() {
        ApiClient.getSupabaseApi().getAuditLogs("created_at.desc", 50).enqueue(new Callback<List<AuditLog>>() {
            @Override
            public void onResponse(@NonNull Call<List<AuditLog>> call, @NonNull Response<List<AuditLog>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    auditAdapter.updateList(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AuditLog>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load audit logs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateKycStatus(User user, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("kyc_status", newStatus);

        ApiClient.getSupabaseApi().updateProfile("eq." + user.getId(), updates).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminDashboardActivity.this, "User " + newStatus, Toast.LENGTH_SHORT).show();
                    fetchPendingKyc(); // Refresh list
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Error updating status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
