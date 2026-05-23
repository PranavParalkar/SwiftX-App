package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progress;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.loginProgress);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- DEVELOPMENT BYPASS: ADMIN LOGIN ---
        if (email.equals("admin@swiftx.ai") && password.equals("swiftx_admin_2026")) {
            Toast.makeText(this, "Admin Authenticated. Provisioning...", Toast.LENGTH_SHORT).show();
            ensureAdminExists(email);
            return;
        }
        // ---------------------------------------

        btnLogin.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        Map<String, String> creds = new HashMap<>();
        creds.put("email", email);
        creds.put("password", password);

        // 1. Real Supabase Auth: Get the JWT token
        ApiClient.getAuthApi().signIn(creds).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String accessToken = (String) response.body().get("access_token");
                    Map<String, Object> userMap = (Map<String, Object>) response.body().get("user");
                    String userId = userMap != null ? (String) userMap.get("id") : null;

                    if (userId != null && accessToken != null) {
                        // 2. Set token in ApiClient for RLS
                        ApiClient.setAuthToken(accessToken);

                        // 3. Fetch full profile from REST API
                        fetchProfile(userId, accessToken);
                    } else {
                        onLoginError("Session data missing from response");
                    }
                } else {
                    String errorMsg = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            Map<String, Object> errorMap = new com.google.gson.Gson().fromJson(
                                    response.errorBody().charStream(), Map.class);
                            if (errorMap != null && errorMap.containsKey("error_description")) {
                                errorMsg = (String) errorMap.get("error_description");
                            } else if (errorMap != null && errorMap.containsKey("msg")) {
                                errorMsg = (String) errorMap.get("msg");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onLoginError(errorMsg);
                }
            }

            private void onLoginError(String msg) {
                btnLogin.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnLogin.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProfile(String userId, String token) {

        ApiClient.getSupabaseApi().getProfile("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                btnLogin.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    sessionManager.saveSession(token, user.getId(), user.getEmail(), user.getFullName());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                btnLogin.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Error fetching profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ensureAdminExists(String email) {
        // We use a valid UUID format to satisfy Postgres UUID type requirements
        // Note: This will still fail if RLS or FK constraints are active without a real Auth user
        String adminId = "00000000-0000-0000-0000-000000000001"; 
        
        ApiClient.getSupabaseApi().getProfile("eq." + adminId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    sessionManager.saveSession("mock_admin_token", adminId, email, "Administrator");
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    createAdminData(adminId, email);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                proceedAnyway(adminId, email, "Network failure - entering Offline Admin mode");
            }
        });
    }

    private void createAdminData(String adminId, String email) {
        User admin = new User();
        admin.setId(adminId);
        admin.setFullName("Administrator");
        admin.setEmail(email);
        admin.setKycStatus("verified");

        ApiClient.getSupabaseApi().createProfile(admin).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    provisionWallet(adminId, email);
                } else {
                    proceedAnyway(adminId, email, "DB Constraint: Please Register 'admin@swiftx.ai' via Sign Up first.");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                proceedAnyway(adminId, email, "Provisioning failed.");
            }
        });
    }

    private void provisionWallet(String adminId, String email) {
        com.example.swift_app.models.Wallet wallet = new com.example.swift_app.models.Wallet();
        wallet.setUserId(adminId);
        wallet.setCurrency("USD");
        wallet.setBalance(1000000.0);

        ApiClient.getSupabaseApi().createWallet(wallet).enqueue(new Callback<List<com.example.swift_app.models.Wallet>>() {
            @Override
            public void onResponse(Call<List<com.example.swift_app.models.Wallet>> call, Response<List<com.example.swift_app.models.Wallet>> response) {
                proceedAnyway(adminId, email, "Admin Provisioned Successfully");
            }

            @Override
            public void onFailure(Call<List<com.example.swift_app.models.Wallet>> call, Throwable t) {
                proceedAnyway(adminId, email, "Wallet setup failed.");
            }
        });
    }

    private void proceedAnyway(String adminId, String email, String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
        sessionManager.saveSession("mock_admin_token", adminId, email, "Administrator");
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

}
