package com.forgequeue.core.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "jobs",
    indexes = {
        @Index(name = "idx_jobs_status_next_run", columnList = "status,next_run_at"),
        @Index(name = "idx_jobs_priority", columnList = "priority"),
        @Index(name = "idx_jobs_lease_expiry", columnList = "lease_expires_at"),
        @Index(name = "idx_jobs_user_status", columnList = "user_id,status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_jobs_user_idempotency",
            columnNames = {"user_id", "idempotency_key"}
        )
    }
)
public class Job extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "type", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(nullable = false)
    private Integer priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private String headers;

    @Column(name = "worker_id")
    private String workerId;

    @Column(name = "lease_expires_at")
    private Instant leaseExpiresAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts;

    @Column(name = "next_run_at", nullable = false)
    private Instant nextRunAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_payload", columnDefinition = "jsonb")
    private String resultPayload;

    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    @Column(name = "last_error_stacktrace", columnDefinition = "text")
    private String lastErrorStacktrace;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Job() {
        // for JPA
    }

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
    }

    // -------- Getters and Setters --------

public UUID getId() { return id; }

public String getUserId() { return userId; }
public void setUserId(String userId) { this.userId = userId; }

public String getIdempotencyKey() { return idempotencyKey; }
public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

public String getType() { return type; }
public void setType(String type) { this.type = type; }

public JobStatus getStatus() { return status; }
public void setStatus(JobStatus status) { this.status = status; }

public Integer getPriority() { return priority; }
public void setPriority(Integer priority) { this.priority = priority; }

public String getPayload() { return payload; }
public void setPayload(String payload) { this.payload = payload; }

public String getHeaders() { return headers; }
public void setHeaders(String headers) { this.headers = headers; }

public String getWorkerId() { return workerId; }
public void setWorkerId(String workerId) { this.workerId = workerId; }

public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
public void setLeaseExpiresAt(Instant leaseExpiresAt) { this.leaseExpiresAt = leaseExpiresAt; }

public Integer getAttemptCount() { return attemptCount; }
public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }

public Integer getMaxAttempts() { return maxAttempts; }
public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

public Instant getNextRunAt() { return nextRunAt; }
public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }

public String getResultPayload() { return resultPayload; }
public void setResultPayload(String resultPayload) { this.resultPayload = resultPayload; }

public String getLastErrorMessage() { return lastErrorMessage; }
public void setLastErrorMessage(String lastErrorMessage) { this.lastErrorMessage = lastErrorMessage; }

public String getLastErrorStacktrace() { return lastErrorStacktrace; }
public void setLastErrorStacktrace(String lastErrorStacktrace) { this.lastErrorStacktrace = lastErrorStacktrace; }

public Instant getFailedAt() { return failedAt; }
public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }

public Instant getCompletedAt() { return completedAt; }
public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
   

}