package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private String id;

    @SerializedName("txn_ref")
    private String txnRef;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("receiver_id")
    private String receiverId;

    @SerializedName("source_currency")
    private String sourceCurrency;

    @SerializedName("target_currency")
    private String targetCurrency;

    @SerializedName("source_amount")
    private double sourceAmount;

    @SerializedName("target_amount")
    private double targetAmount;

    @SerializedName("fx_rate")
    private double fxRate;

    @SerializedName("fee_amount")
    private double feeAmount;

    @SerializedName("fee_currency")
    private String feeCurrency;

    @SerializedName("status")
    private String status; // pending, processing, completed, failed, reversed

    @SerializedName("note")
    private String note;

    @SerializedName("locked_rate_at")
    private String lockedRateAt;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Nested sender/receiver profiles (from join)
    @SerializedName("sender")
    private User sender;

    @SerializedName("receiver")
    private User receiver;

    // Legacy fields for backward compatibility
    private String currentHash;
    private String prevHash;

    public Transaction() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTxnRef() { return txnRef; }
    public void setTxnRef(String txnRef) { this.txnRef = txnRef; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    // Legacy compatibility
    public String getRecipientId() { return receiverId; }
    public void setRecipientId(String recipientId) { this.receiverId = recipientId; }

    public String getSourceCurrency() { return sourceCurrency; }
    public void setSourceCurrency(String sourceCurrency) { this.sourceCurrency = sourceCurrency; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public double getSourceAmount() { return sourceAmount; }
    public void setSourceAmount(double sourceAmount) { this.sourceAmount = sourceAmount; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    // Legacy compatibility
    public double getAmount() { return sourceAmount; }
    public void setAmount(double amount) { this.sourceAmount = amount; }

    public String getCurrency() { return sourceCurrency; }
    public void setCurrency(String currency) { this.sourceCurrency = currency; }

    public double getConvertedAmount() { return targetAmount; }
    public void setConvertedAmount(double convertedAmount) { this.targetAmount = convertedAmount; }

    public double getFxRate() { return fxRate; }
    public void setFxRate(double fxRate) { this.fxRate = fxRate; }

    public double getFeeAmount() { return feeAmount; }
    public void setFeeAmount(double feeAmount) { this.feeAmount = feeAmount; }

    public String getFeeCurrency() { return feeCurrency; }
    public void setFeeCurrency(String feeCurrency) { this.feeCurrency = feeCurrency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getLockedRateAt() { return lockedRateAt; }
    public void setLockedRateAt(String lockedRateAt) { this.lockedRateAt = lockedRateAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    // Legacy hash chain support
    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    // Legacy compatibility methods
    public String getType() {
        if (senderId != null && receiverId != null && senderId.equals(receiverId)) {
            return "deposit";
        }
        return "transfer";
    }

    public void setType(String type) {
        // Type is derived, not stored
    }

    public String getSenderName() {
        return sender != null ? sender.getFullName() : null;
    }

    public void setSenderName(String senderName) {
        // Derived from sender object
    }

    public String getRecipientName() {
        return receiver != null ? receiver.getFullName() : null;
    }

    public void setRecipientName(String recipientName) {
        // Derived from receiver object
    }

    // Helper methods
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
            return receiver != null ? receiver.getFullName() : "Unknown";
        }
        return sender != null ? sender.getFullName() : "Unknown";
    }

    public String getFormattedAmount(String currentUserId) {
        String sign = isSent(currentUserId) ? "-" : "+";
        return sign + getCurrencySymbol() + String.format("%,.2f", sourceAmount);
    }

    public String getCurrencySymbol() {
        switch (sourceCurrency != null ? sourceCurrency : "") {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "INR": return "₹";
            case "JPY": return "¥";
            case "PHP": return "₱";
            case "AED": return "د.إ";
            default: return sourceCurrency != null ? sourceCurrency + " " : "$";
        }
    }
}
