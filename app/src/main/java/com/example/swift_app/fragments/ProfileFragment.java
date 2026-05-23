package com.example.swift_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.swift_app.R;
import com.example.swift_app.activities.LoginActivity;
import com.example.swift_app.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        sessionManager = new SessionManager(requireContext());

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvInitial = view.findViewById(R.id.tvProfileInitial);
        
        String name = sessionManager.getUserName();
        tvName.setText(name);
        tvEmail.setText(sessionManager.getUserEmail());

        if (name != null && !name.isEmpty()) {
            tvInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            tvInitial.setText("U");
        }

        view.findViewById(R.id.cvVerifyKyc).setOnClickListener(v -> {
            startActivity(new android.content.Intent(requireContext(), com.example.swift_app.activities.KycActivity.class));
        });

        view.findViewById(R.id.btnSecuritySettings).setOnClickListener(v -> {
            // Placeholder for now
        });

        view.findViewById(R.id.btnHelpSupport).setOnClickListener(v -> {
            // Placeholder for now
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.clearSession();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
