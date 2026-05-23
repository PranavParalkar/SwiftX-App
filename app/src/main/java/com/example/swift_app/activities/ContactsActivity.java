package com.example.swift_app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swift_app.R;
import com.example.swift_app.adapters.ContactAdapter;
import com.example.swift_app.models.User;
import com.example.swift_app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView rvContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fetchContacts();
    }

    private void fetchContacts() {
        // For this demo, we fetch all profiles as potential contacts
        ApiClient.getSupabaseApi().getProfiles().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ContactAdapter adapter = new ContactAdapter(response.body(), contact -> {
                        Intent result = new Intent();
                        result.putExtra("selected_email", contact.getEmail());
                        setResult(RESULT_OK, result);
                        finish();
                    });
                    rvContacts.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                // Error handling could be added here
            }
        });
    }
}
