package com.forgequeue.core.dto;

public class JobSubmitRequest {

    private String userId;
    private String idempotencyKey;
    private String type;
    private Integer priority;
    private String payload;

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

    public String getPayload() {
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

    public void setPayload(String payload) {
        this.payload = payload;
    }
}