package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class FxRate {
    @SerializedName("id")
    private String id;

    @SerializedName("base_currency")
    private String baseCurrency;

    @SerializedName("target_currency")
    private String targetCurrency;

    @SerializedName("rate")
    private double rate;

    @SerializedName("fetched_at")
    private String fetchedAt;

    public FxRate() {}

    public FxRate(String baseCurrency, String targetCurrency, double rate) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getTargetCurrency() { return targetCurrency; }
    public void setTargetCurrency(String targetCurrency) { this.targetCurrency = targetCurrency; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public String getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(String fetchedAt) { this.fetchedAt = fetchedAt; }

    public double convert(double amount) {
        return amount * rate;
    }

    public String getDisplayRate() {
        return "1 " + baseCurrency + " = " + String.format("%.4f", rate) + " " + targetCurrency;
    }
}
