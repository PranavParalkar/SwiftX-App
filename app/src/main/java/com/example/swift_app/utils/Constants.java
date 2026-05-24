package com.example.swift_app.utils;

public class Constants {
    // Supabase - PRODUCTION CONFIGURATION
    // TODO: Replace with your actual Supabase project credentials from https://supabase.com/dashboard/project/_/settings/api
    public static final String SUPABASE_URL = "https://hpyjaaaqppntztlfizxp.supabase.co";
    public static final String SUPABASE_ANON_KEY = "sb_publishable_Kp1XbDZmWAcOq_lNnCdRCg_MbHy250p"; // ⚠️ REPLACE THIS!

    // API endpoints
    public static final String FX_API_BASE = "https://api.exchangerate-api.com/v4/";
    public static final String CLAUDE_API_BASE = "https://api.anthropic.com/";
    public static final String CLAUDE_API_KEY = ""; // Optional: Add Claude API key for AI insights

    // Polygon
    public static final String POLYGON_RPC = "https://rpc-amoy.polygon.technology/";
    public static final String ANCHOR_CONTRACT = ""; // Optional: Add contract address for blockchain anchoring

    // SharedPreferences
    public static final String PREF_NAME = "swift_prefs";
    public static final String PREF_TOKEN = "auth_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_ONBOARDING_DONE = "onboarding_done";
    public static final String PREF_PREFERRED_CURRENCY = "preferred_currency";
    public static final String PREF_LANGUAGE = "language";

    // Transaction types
    public static final String TYPE_TRANSFER = "transfer";
    public static final String TYPE_DEPOSIT = "deposit";
    public static final String TYPE_WITHDRAWAL = "withdrawal";

    // Transaction statuses
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    // KYC statuses
    public static final String KYC_PENDING = "pending";
    public static final String KYC_SUBMITTED = "submitted";
    public static final String KYC_VERIFIED = "verified";
    public static final String KYC_REJECTED = "rejected";

    // AML thresholds
    public static final double AML_AMOUNT_THRESHOLD = 10000.0;
    public static final int AML_VELOCITY_LIMIT = 5; // max transactions per hour
    public static final double AML_DAILY_LIMIT = 50000.0;

    // FX
    public static final double FX_SPREAD = 0.002; // 0.2% margin
    public static final long FX_CACHE_DURATION_MS = 3600000; // 1 hour

    // Currencies supported
    public static final String[] CURRENCIES = {
        "USD", "EUR", "GBP", "INR", "JPY", "PHP", "MXN", "BRL", "NGN", "KES"
    };

    public static final String[] CURRENCY_NAMES = {
        "US Dollar", "Euro", "British Pound", "Indian Rupee", "Japanese Yen",
        "Philippine Peso", "Mexican Peso", "Brazilian Real", "Nigerian Naira", "Kenyan Shilling"
    };

    // Countries
    public static final String[] COUNTRIES = {
        "United States", "United Kingdom", "India", "Philippines", "Mexico",
        "Brazil", "Nigeria", "Kenya", "Japan", "Germany", "France", "Canada",
        "Australia", "South Korea", "Bangladesh", "Pakistan", "Nepal", "Sri Lanka"
    };
}
