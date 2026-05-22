package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private String id;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("recipient_id")
    private String recipientId;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("recipient_name")
    private String recipientName;

    @SerializedName("amount")
    private double amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("converted_amount")
    private double convertedAmount;

    @SerializedName("target_currency")
    private String targetCurrency;

    @SerializedName("fx_rate")
    private double fxRate;

    @SerializedName("status")
    private String status; // pending, completed, failed

    @SerializedName("type")
    private String type; // transfer, deposit, withdrawal

    @SerializedName("prev_hash")
    private String prevHash;

    @SerializedName("current_hash")
    private String currentHash;

    @SerializedName("created_at")
    private String createdAt;

    public Transaction() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(double convertedAmount) { this.convertedAmount = convertedAmount; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public double getFxRate() { return fxRate; }
    public void setFxRate(double fxRate) { this.fxRate = fxRate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isSent(String userId) {
        return userId != null && userId.equals(senderId);
    }

    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status);
    }

    public String getDisplayName(String currentUserId) {
        if (isSent(currentUserId)) {
            return recipientName != null ? recipientName : "Unknown";
        }
        return senderName != null ? senderName : "Unknown";
    }

    public String getFormattedAmount(String currentUserId) {
        String sign = isSent(currentUserId) ? "-" : "+";
        return sign + getCurrencySymbol() + String.format("%,.2f", amount);
    }

    public String getCurrencySymbol() {
        switch (currency != null ? currency : "") {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "INR": return "₹";
            case "JPY": return "¥";
            case "PHP": return "₱";
            default: return currency != null ? currency + " " : "$";
        }
    }
}
