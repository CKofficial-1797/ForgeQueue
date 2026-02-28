package com.forgequeue.core;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.repository.JobRepository;
import com.forgequeue.core.worker.JobLeasingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CrashRecoveryIT extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLeasingService leasingService;

    @Test
    void shouldReclaimJobAfterLeaseExpires() throws Exception {

        Job job = new Job();
        job.setUserId("user-1");
        job.setIdempotencyKey(UUID.randomUUID().toString());
        job.setType("EMAIL");
        job.setStatus(JobStatus.QUEUED);
        job.setPriority(5);
        job.setPayload(Map.of("to", "a@b.com"));
        job.setAttemptCount(0);
        job.setMaxAttempts(3);
        job.setNextRunAt(Instant.now());

        jobRepository.save(job);

        // First lease
        leasingService.leaseBatch();

        Job leased = jobRepository.findAll().get(0);
        assertThat(leased.getStatus()).isEqualTo(JobStatus.PROCESSING);

        // Simulate lease expiry
        leased.setLeaseExpiresAt(Instant.now().minusSeconds(5));
        jobRepository.save(leased);

        // Second lease attempt
        leasingService.leaseBatch();

        Job reclaimed = jobRepository.findAll().get(0);

        assertThat(reclaimed.getAttemptCount()).isEqualTo(2);
        assertThat(reclaimed.getStatus()).isEqualTo(JobStatus.PROCESSING);
    }
}