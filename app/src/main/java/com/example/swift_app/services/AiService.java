package com.example.swift_app.services;

import com.example.swift_app.utils.Constants;

import java.util.Random;

public class AiService {

    private static final String[] MOCK_INSIGHTS = {
        "Based on your spending patterns, you could save $120/month by optimizing your transfer schedule.",
        "Your transaction integrity score is 99.8%. Your account is highly secure.",
        "Global FX rates for INR are favorable today. Good time to send money!",
        "You've saved $45 in fees this month compared to traditional banks.",
        "Tip: Setting up a recurring transfer to your savings wallet can boost your interest by 2%."
    };

    public interface AiCallback {
        void onInsightGenerated(String insight);
    }

    public void getPersonalizedInsight(String userId, AiCallback callback) {
        // If API key is provided, this would hit Claude API via Retrofit
        // For now, we return a smart mock to demonstrate the feature
        if (Constants.CLAUDE_API_KEY.isEmpty()) {
            int index = new Random().nextInt(MOCK_INSIGHTS.length);
            callback.onInsightGenerated(MOCK_INSIGHTS[index]);
        } else {
            // Real Claude API integration logic would go here
            callback.onInsightGenerated(MOCK_INSIGHTS[0]);
        }
    }
}
