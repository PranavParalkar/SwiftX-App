package com.example.swift_app.fragments;

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

import com.example.swift_app.R;
import com.example.swift_app.activities.KycActivity;
import com.example.swift_app.activities.LoginActivity;
import com.example.swift_app.utils.SessionManager;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        SessionManager sessionManager = new SessionManager(requireContext());

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvInitial = view.findViewById(R.id.tvProfileInitial);
        
        String name = sessionManager.getUserName();
        tvName.setText(name);
        tvEmail.setText(sessionManager.getUserEmail());

        if (name != null && !name.isEmpty()) {
            tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase(Locale.getDefault()));
        } else {
            tvInitial.setText("U");
        }

        view.findViewById(R.id.cvVerifyKyc).setOnClickListener(v -> 
            startActivity(new Intent(requireContext(), KycActivity.class)));

        view.findViewById(R.id.btnSecuritySettings).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Security settings coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnHelpSupport).setOnClickListener(v -> 
            Toast.makeText(requireContext(), "Help & Support coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
