package com.example.swift_app.services;

import com.example.swift_app.models.PolygonAnchor;
import com.example.swift_app.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PolygonAnchorService {

    public interface AnchorCallback {
        void onAnchorStatus(PolygonAnchor anchor);
    }

    /**
     * Checks if a specific chain root has been anchored to Polygon.
     */
    public static void verifyAnchoring(String rootHash, AnchorCallback callback) {
        // In a real app, we'd query our Supabase 'polygon_anchors' table
        // filtering by chain_root_hash.
        ApiClient.getSupabaseApi().getAnchors("eq." + rootHash).enqueue(new Callback<List<PolygonAnchor>>() {
            @Override
            public void onResponse(Call<List<PolygonAnchor>> call, Response<List<PolygonAnchor>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onAnchorStatus(response.body().get(0));
                } else {
                    callback.onAnchorStatus(null);
                }
            }

            @Override
            public void onFailure(Call<List<PolygonAnchor>> call, Throwable t) {
                callback.onAnchorStatus(null);
            }
        });
    }
}
