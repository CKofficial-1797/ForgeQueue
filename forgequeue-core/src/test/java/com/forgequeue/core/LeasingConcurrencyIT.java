package com.forgequeue.core;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.repository.JobRepository;
import com.forgequeue.core.worker.JobLeasingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class LeasingConcurrencyIT extends AbstractIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLeasingService leasingService;

    @Test
    void shouldNotLeaseSameJobTwiceUnderConcurrency() throws Exception {

        // Create 3 queued jobs
        for (int i = 0; i < 3; i++) {
            Job job = new Job();
            job.setUserId("user-" + i);
            job.setIdempotencyKey(UUID.randomUUID().toString());
            job.setType("EMAIL");
            job.setStatus(JobStatus.QUEUED);
            job.setPriority(5);
            job.setPayload(Map.of("to", "a@b.com"));
            job.setAttemptCount(0);
            job.setMaxAttempts(3);
            job.setNextRunAt(Instant.now());

            jobRepository.save(job);
        }

        int threads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        Set<UUID> leasedJobIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    List<Job> leased = leasingService.leaseBatch();
                    leased.forEach(job -> leasedJobIds.add(job.getId()));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(leasedJobIds).hasSize(3);
    }
}