package com.forgequeue.core.dto;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;

import java.time.Instant;
import java.util.UUID;

public class JobSubmitResponse {

    private UUID id;
    private String userId;
    private String type;
    private JobStatus status;
    private Integer priority;
    private Instant createdAt;
    private Instant nextRunAt;

    public static JobSubmitResponse from(Job job) {
        JobSubmitResponse response = new JobSubmitResponse();
        response.id = job.getId();
        response.userId = job.getUserId();
        response.type = job.getType();
        response.status = job.getStatus();
        response.priority = job.getPriority();
        response.createdAt = job.getCreatedAt();
        response.nextRunAt = job.getNextRunAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Integer getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }
}