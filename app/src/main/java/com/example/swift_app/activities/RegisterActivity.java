package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.Constants;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private AutoCompleteTextView actvCountry, actvCurrency;
    private Button btnRegister;
    private ProgressBar progress;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        initViews();
        setupDropdowns();

        btnRegister.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.tvSignIn).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        actvCountry = findViewById(R.id.actvCountry);
        actvCurrency = findViewById(R.id.actvCurrency);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.registerProgress);
    }

    private void setupDropdowns() {
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.COUNTRIES);
        actvCountry.setAdapter(countryAdapter);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.CURRENCIES);
        actvCurrency.setAdapter(currencyAdapter);
    }

    private void attemptRegister() {
        if (etFullName.getText() == null || etEmail.getText() == null || 
            etPassword.getText() == null || etConfirmPassword.getText() == null) return;

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        // 1. First, register user in Supabase Auth (GoTrue)
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        ApiClient.getAuthApi().signUp(credentials).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) body.get("user");
                    String userId = userMap != null ? (String) userMap.get("id") : null;
                    String accessToken = (String) body.get("access_token");

                    if (userId != null) {
                        if (accessToken != null) {
                            ApiClient.setAuthToken(accessToken);
                        }
                        createDataProfile(userId, name, email, accessToken);
                    } else {
                        onAuthError("User ID missing from response");
                    }
                } else {
                    String errorMsg = "Registration failed";
                    try (ResponseBody errorBody = response.errorBody()) {
                        if (errorBody != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> errorMap = new com.google.gson.Gson().fromJson(errorBody.charStream(), Map.class);
                            if (errorMap != null && errorMap.containsKey("msg")) {
                                errorMsg = (String) errorMap.get("msg");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing registration failure", e);
                    }
                    onAuthError(errorMsg);
                }
            }

            private void onAuthError(String msg) {
                btnRegister.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                btnRegister.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDataProfile(String userId, String name, String email, String token) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setFullName(name);
        user.setRole("user");
        user.setKycStatus("pending");
        user.setPhone(etPhone.getText() != null ? etPhone.getText().toString() : "");
        user.setCountry(actvCountry.getText() != null ? actvCountry.getText().toString() : "");

        Log.d(TAG, "Creating profile for user: " + email + " (ID: " + userId + ")");
        Log.d(TAG, "Profile data: " + user.getFullName() + ", " + user.getCountry() + ", " + user.getPhone());

        ApiClient.getSupabaseApi().createProfile(user).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Profile created successfully");
                    createUnifiedWallet(userId, email, name, token);
                } else {
                    // Profile creation failed - need to clean up the auth user
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    
                    Log.e(TAG, "Profile creation failed: " + response.code() + " - " + response.message());
                    Log.e(TAG, "Error body: " + errorBody);
                    cleanupFailedRegistration(userId, email, "Profile creation failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error during profile creation", t);
                cleanupFailedRegistration(userId, email, "Network error: " + t.getMessage());
            }
        });
    }

    private void cleanupFailedRegistration(String userId, String email, String error) {
        // In a real app, you would need admin privileges to delete the auth user
        // For now, we'll just show a helpful message
        btnRegister.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        
        String message = "Registration failed: " + error + 
                        "\n\nAuth user was created but profile setup failed. " +
                        "You can try to login with these credentials, " +
                        "but you may need to contact support.";
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
        
        // Log the issue for debugging
        Log.w(TAG, "Zombie user created: " + email + " (ID: " + userId + ") - Error: " + error);
    }

    private void createUnifiedWallet(String userId, String email, String fullName, String token) {
        com.example.swift_app.models.Wallet wallet = new com.example.swift_app.models.Wallet();
        wallet.setUserId(userId);
        wallet.setInrBalance(0.0);
        wallet.setUsdBalance(0.0);
        wallet.setAedBalance(0.0);

        Log.d(TAG, "Creating wallet for user: " + userId);

        ApiClient.getSupabaseApi().createWallet(wallet).enqueue(new Callback<List<com.example.swift_app.models.Wallet>>() {
            @Override
            public void onResponse(@NonNull Call<List<com.example.swift_app.models.Wallet>> call, @NonNull Response<List<com.example.swift_app.models.Wallet>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Wallet created successfully");
                    sessionManager.saveSession(token != null ? token : "mock_token", userId, email, fullName);
                    Toast.makeText(RegisterActivity.this, "Welcome to SwiftX!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading wallet error body", e);
                    }
                    
                    Log.e(TAG, "Wallet creation failed: " + response.code() + " - " + response.message());
                    Log.e(TAG, "Error body: " + errorBody);
                    
                    // Even if wallet fails, we can still login - wallet can be created later
                    sessionManager.saveSession(token != null ? token : "mock_token", userId, email, fullName);
                    Toast.makeText(RegisterActivity.this, 
                        "Account created! Wallet setup will complete shortly.", 
                        Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<com.example.swift_app.models.Wallet>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error during wallet creation", t);
                
                // Proceed anyway - wallet can be provisioned by admin if it fails
                sessionManager.saveSession(token != null ? token : "mock_token", userId, email, fullName);
                Toast.makeText(RegisterActivity.this, 
                    "Account created! Please check your wallet in a few minutes.", 
                    Toast.LENGTH_LONG).show();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
