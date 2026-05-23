package com.example.swift_app.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class AuditLog {
    @SerializedName("id")
    private String id;

    @SerializedName("actor_id")
    private String actorId;

    @SerializedName("action")
    private String action;

    @SerializedName("entity")
    private String entity;

    @SerializedName("entity_id")
    private String entityId;

    @SerializedName("meta")
    private Map<String, Object> meta;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getAction() { return action; }
    public String getEntity() { return entity; }
    public String getCreatedAt() { return createdAt; }
    public String getEntityId() { return entityId; }
    public Map<String, Object> getMeta() { return meta; }
}
