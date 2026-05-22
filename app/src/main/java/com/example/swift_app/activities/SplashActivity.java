package com.example.swift_app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swift_app.R;
import com.example.swift_app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        final View splashContent = findViewById(R.id.splashContent);
        final View splashTagline = findViewById(R.id.splashTagline);

        // Simple fade in animation
        splashContent.animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashTagline.animate().alpha(1f).setDuration(500).start();
                        
                        splashContent.postDelayed(() -> {
                            startNextActivity();
                        }, 1000);
                    }
                });
    }

    private void startNextActivity() {
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else if (sessionManager.isOnboardingDone()) {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
