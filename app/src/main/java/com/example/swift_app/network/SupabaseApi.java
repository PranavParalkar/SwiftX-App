package com.example.swift_app.network;

import com.example.swift_app.models.Transaction;
import com.example.swift_app.models.User;
import com.example.swift_app.models.Wallet;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // Profiles
    @GET("profiles")
    Call<List<User>> getProfile(@Query("id") String idFilter);

    @POST("profiles")
    Call<List<User>> createProfile(@Body User user);

    @PATCH("profiles")
    Call<List<User>> updateProfile(@Query("id") String idFilter, @Body Map<String, Object> updates);

    // Wallets
    @GET("wallets")
    Call<List<Wallet>> getWallets(@Query("user_id") String userIdFilter);

    @POST("wallets")
    Call<List<Wallet>> createWallet(@Body Wallet wallet);

    @PATCH("wallets")
    Call<List<Wallet>> updateWallet(@Query("id") String idFilter, @Body Map<String, Object> updates);

    // Transactions
    @GET("transactions")
    Call<List<Transaction>> getTransactions(
            @Query("or") String orFilter,
            @Query("order") String order,
            @Query("limit") int limit
    );

    @POST("transactions")
    Call<List<Transaction>> createTransaction(@Body Transaction transaction);

    @PATCH("transactions")
    Call<List<Transaction>> updateTransaction(@Query("id") String idFilter, @Body Map<String, Object> updates);

    // Auth (Supabase GoTrue)
    @POST("signup")
    Call<Map<String, Object>> signUp(@Body Map<String, String> credentials);

    @POST("token?grant_type=password")
    Call<Map<String, Object>> signIn(@Body Map<String, String> credentials);
}

