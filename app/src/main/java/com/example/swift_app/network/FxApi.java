package com.example.swift_app.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FxApi {
    // Using exchangerate-api.com which provides free tier without API key
    // Format: https://api.exchangerate-api.com/v4/latest/USD
    @GET("latest/{base}")
    Call<Map<String, Object>> getLatestRates(@Path("base") String base);
}
