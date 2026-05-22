package com.example.swift_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Auth Token
    public void saveToken(String token) {
        editor.putString(Constants.PREF_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(Constants.PREF_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    // User Info
    public void saveUserId(String userId) {
        editor.putString(Constants.PREF_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.PREF_USER_ID, null);
    }

    public void saveUserEmail(String email) {
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return prefs.getString(Constants.PREF_USER_EMAIL, null);
    }

    public void saveUserName(String name) {
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.apply();
    }

    public String getUserName() {
        return prefs.getString(Constants.PREF_USER_NAME, "User");
    }

    // Onboarding
    public void setOnboardingDone(boolean done) {
        editor.putBoolean(Constants.PREF_ONBOARDING_DONE, done);
        editor.apply();
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(Constants.PREF_ONBOARDING_DONE, false);
    }

    // Currency
    public void savePreferredCurrency(String currency) {
        editor.putString(Constants.PREF_PREFERRED_CURRENCY, currency);
        editor.apply();
    }

    public String getPreferredCurrency() {
        return prefs.getString(Constants.PREF_PREFERRED_CURRENCY, "USD");
    }

    // Language
    public void saveLanguage(String lang) {
        editor.putString(Constants.PREF_LANGUAGE, lang);
        editor.apply();
    }

    public String getLanguage() {
        return prefs.getString(Constants.PREF_LANGUAGE, "en");
    }

    // Session management
    public void saveSession(String token, String userId, String email, String name) {
        editor.putString(Constants.PREF_TOKEN, token);
        editor.putString(Constants.PREF_USER_ID, userId);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putString(Constants.PREF_USER_NAME, name);
        editor.apply();
    }

    public void clearSession() {
        editor.remove(Constants.PREF_TOKEN);
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
