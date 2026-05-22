package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class Wallet {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("currency")
    private String currency;

    @SerializedName("balance")
    private double balance;

    @SerializedName("created_at")
    private String createdAt;

    public Wallet() {}

    public Wallet(String id, String userId, String currency, double balance) {
        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCurrencySymbol() {
        switch (currency != null ? currency : "") {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "INR": return "₹";
            case "JPY": return "¥";
            case "PHP": return "₱";
            case "MXN": return "$";
            case "BRL": return "R$";
            case "NGN": return "₦";
            case "KES": return "KSh";
            default: return currency != null ? currency : "$";
        }
    }

    public String getFormattedBalance() {
        return getCurrencySymbol() + String.format("%,.2f", balance);
    }

    public String getCurrencyFlag() {
        switch (currency != null ? currency : "") {
            case "USD": return "🇺🇸";
            case "EUR": return "🇪🇺";
            case "GBP": return "🇬🇧";
            case "INR": return "🇮🇳";
            case "JPY": return "🇯🇵";
            case "PHP": return "🇵🇭";
            case "MXN": return "🇲🇽";
            case "BRL": return "🇧🇷";
            case "NGN": return "🇳🇬";
            case "KES": return "🇰🇪";
            default: return "🌍";
        }
    }
}
