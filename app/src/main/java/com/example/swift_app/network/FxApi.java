package com.example.swift_app.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FxApi {
    @GET("latest")
    Call<Map<String, Object>> getLatestRates(@Query("base") String base);

    @GET("convert")
    Call<Map<String, Object>> convert(
        @Query("from") String from,
        @Query("to") String to,
        @Query("amount") double amount
    );
}
