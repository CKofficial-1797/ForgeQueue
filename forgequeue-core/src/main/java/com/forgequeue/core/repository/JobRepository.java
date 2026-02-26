package com.forgequeue.core.repository;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import java.util.Optional;

Optional<Job> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    boolean existsByUserIdAndIdempotencyKey(String userId, String idempotencyKey);

    @Query(value = """
        SELECT *
        FROM jobs
        WHERE status IN ('QUEUED', 'RETRY_WAIT')
          AND next_run_at <= :now
        ORDER BY priority DESC, created_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true)
    List<Job> findEligibleJobsForLeasing(
            @Param("now") Instant now,
            @Param("limit") int limit
    );
}