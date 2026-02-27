package com.forgequeue.core.dto;

import java.util.Map;

public class JobSubmitRequest {

    private String userId;
    private String idempotencyKey;
    private String type;
    private Integer priority;
    private Map<String, Object> payload;

    public String getUserId() {
        return userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getType() {
        return type;
    }

    public Integer getPriority() {
        return priority;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}