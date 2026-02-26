package com.forgequeue.core.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_jobs")
public class DeadLetterJob extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "original_job_id", nullable = false)
    private UUID originalJobId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "type", nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    public DeadLetterJob() {
    }

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID();
    }
}