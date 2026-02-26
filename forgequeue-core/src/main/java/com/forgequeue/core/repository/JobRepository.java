package com.forgequeue.core.repository;

import com.forgequeue.core.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);

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