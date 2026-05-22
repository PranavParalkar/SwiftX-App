package com.example.swift_app.network;

import com.example.swift_app.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit supabaseRetrofit = null;
    private static Retrofit fxRetrofit = null;
    private static Retrofit claudeRetrofit = null;
    private static String currentAuthToken = Constants.SUPABASE_ANON_KEY;

    public static void setAuthToken(String token) {
        currentAuthToken = token != null ? token : Constants.SUPABASE_ANON_KEY;
        // Reset retrofit to apply new token
        supabaseRetrofit = null;
    }

    private static OkHttpClient getClient(String apiKey, boolean isSupabase) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging);

        if (isSupabase) {
            builder.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                okhttp3.Request request = original.newBuilder()
                        .header("apikey", Constants.SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer " + currentAuthToken)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=representation")
                        .build();
                return chain.proceed(request);
            });
        }

        return builder.build();
    }

    public static Retrofit getSupabaseClient() {
        if (supabaseRetrofit == null) {
            supabaseRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL + "/rest/v1/")
                    .client(getClient(Constants.SUPABASE_ANON_KEY, true))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return supabaseRetrofit;
    }


    public static Retrofit getFxClient() {
        if (fxRetrofit == null) {
            fxRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.FX_API_BASE)
                    .client(getClient(null, false))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return fxRetrofit;
    }

    public static Retrofit getClaudeClient() {
        if (claudeRetrofit == null) {
            claudeRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.CLAUDE_API_BASE)
                    .client(getClient(null, false))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return claudeRetrofit;
    }

    private static Retrofit authRetrofit = null;
    public static Retrofit getAuthClient() {
        if (authRetrofit == null) {
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SUPABASE_URL + "/auth/v1/")
                    .client(getClient(Constants.SUPABASE_ANON_KEY, true))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static SupabaseApi getSupabaseApi() {
        return getSupabaseClient().create(SupabaseApi.class);
    }

    public static SupabaseApi getAuthApi() {
        return getAuthClient().create(SupabaseApi.class);
    }
}

