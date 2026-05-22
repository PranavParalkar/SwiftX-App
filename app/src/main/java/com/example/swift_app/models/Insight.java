package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class Insight {
    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type; // savings_tip, investment_tip, spending_alert

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("icon")
    private String icon;

    @SerializedName("action_text")
    private String actionText;

    @SerializedName("created_at")
    private String createdAt;

    public Insight() {}

    public Insight(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isSavingsTip() { return "savings_tip".equalsIgnoreCase(type); }
    public boolean isInvestmentTip() { return "investment_tip".equalsIgnoreCase(type); }
    public boolean isSpendingAlert() { return "spending_alert".equalsIgnoreCase(type); }
}
