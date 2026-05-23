package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;

public class PolygonAnchor {
    @SerializedName("id")
    private String id;

    @SerializedName("chain_root_hash")
    private String chainRootHash;

    @SerializedName("polygon_tx_hash")
    private String polygonTxHash;

    @SerializedName("block_number")
    private long blockNumber;

    @SerializedName("anchored_at")
    private String anchoredAt;

    // Getters and Setters
    public String getPolygonTxHash() { return polygonTxHash; }
    public long getBlockNumber() { return blockNumber; }
    public String getAnchoredAt() { return anchoredAt; }
}
