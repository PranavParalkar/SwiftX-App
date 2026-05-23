package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;
import com.example.swift_app.utils.Constants;
import com.example.swift_app.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

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
        Map<String, String> creds = new HashMap<>();
        creds.put("email", email);
        creds.put("password", password);

        ApiClient.getAuthApi().signUp(creds).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> userMap = (Map<String, Object>) response.body().get("user");
                    String userId = userMap != null ? (String) userMap.get("id") : null;
                    String accessToken = (String) response.body().get("access_token");

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
                    try {
                        if (response.errorBody() != null) {
                            Map<String, Object> errorMap = new com.google.gson.Gson().fromJson(
                                    response.errorBody().charStream(), Map.class);
                            if (errorMap != null && errorMap.containsKey("msg")) {
                                errorMsg = (String) errorMap.get("msg");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnRegister.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDataProfile(String userId, String name, String email, String token) {

        User newUser = new User();
        newUser.setId(userId); // Use the Auth ID
        newUser.setFullName(name);
        newUser.setEmail(email);
        newUser.setPhone(etPhone.getText().toString());
        newUser.setCountry(actvCountry.getText().toString());
        newUser.setPreferredCurrency(actvCurrency.getText().toString());
        newUser.setKycStatus(Constants.KYC_PENDING);

        ApiClient.getSupabaseApi().createProfile(newUser).enqueue(new Callback<java.util.List<User>>() {
            @Override
            public void onResponse(Call<java.util.List<User>> call, Response<java.util.List<User>> response) {
                btnRegister.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    sessionManager.saveSession(token, userId, email, name);
                    createDefaultWallet(userId, token);
                } else {
                    Toast.makeText(RegisterActivity.this, "Profile creation failed (check RLS)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.List<User>> call, Throwable t) {
                btnRegister.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Network error during profile creation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDefaultWallet(String userId, String token) {
        com.example.swift_app.models.Wallet defaultWallet = new com.example.swift_app.models.Wallet();
        defaultWallet.setUserId(userId);
        defaultWallet.setCurrency(actvCurrency.getText().toString().isEmpty() ? "USD" : actvCurrency.getText().toString());
        defaultWallet.setBalance(100.0); // Start with $100 for demo purposes

        ApiClient.getSupabaseApi().createWallet(defaultWallet).enqueue(new Callback<java.util.List<com.example.swift_app.models.Wallet>>() {
            @Override
            public void onResponse(Call<java.util.List<com.example.swift_app.models.Wallet>> call, Response<java.util.List<com.example.swift_app.models.Wallet>> response) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<java.util.List<com.example.swift_app.models.Wallet>> call, Throwable t) {
                // Still proceed to main, the user can create a wallet later or retry
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }


}
