package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class Wallet {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("inr_balance")
    private double inrBalance;

    @SerializedName("usd_balance")
    private double usdBalance;

    @SerializedName("aed_balance")
    private double aedBalance;

    @SerializedName("savings_balance")
    private double savingsBalance;

    @SerializedName("created_at")
    private String createdAt;

    // Transient fields for UI/Adapter compatibility
    private String currency;
    private double balance;

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

    public double getInrBalance() { return inrBalance; }
    public void setInrBalance(double inrBalance) { this.inrBalance = inrBalance; }

    public double getUsdBalance() { return usdBalance; }
    public void setUsdBalance(double usdBalance) { this.usdBalance = usdBalance; }

    public double getAedBalance() { return aedBalance; }
    public void setAedBalance(double aedBalance) { this.aedBalance = aedBalance; }

    public double getSavingsBalance() { return savingsBalance; }
    public void setSavingsBalance(double savingsBalance) { this.savingsBalance = savingsBalance; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCurrency() { return currency != null ? currency : "USD"; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCurrencySymbol() {
        switch (getCurrency()) {
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
            default: return "$";
        }
    }

    public String getFormattedBalance() {
        return getCurrencySymbol() + String.format("%,.2f", getBalance());
    }

    public String getCurrencyFlag() {
        switch (getCurrency()) {
            case "USD": return "🇺🇸";
            case "EUR": return "🇪🇺";
            case "GBP": return "🇬🇧";
            case "INR": return "🇮🇳";
            case "JPY": return "🇯🇵";
            case "PHP": return "🇵🇭";
            case "🇲🇽": return "MXN"; // Fixed wait, this was backwards in original
            case "MXN": return "🇲🇽";
            case "BRL": return "🇧🇷";
            case "NGN": return "🇳🇬";
            case "KES": return "🇰🇪";
            default: return "🌍";
        }
    }
}
