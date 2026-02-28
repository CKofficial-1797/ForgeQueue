package com.forgequeue.core;

import com.forgequeue.core.repository.JobRepository;
import com.forgequeue.core.service.JobSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentIdempotencyIT extends AbstractIntegrationTest {

    @Autowired
    private JobSubmissionService submissionService;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void shouldCreateOnlyOneJobUnderConcurrentSubmission() throws Exception {

        String userId = "user-1";
        String key = "race-key";

        int threads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    submissionService.submitJob(
                            userId,
                            key,
                            "EMAIL",
                            5,
                            Map.of("to", "a@b.com")
                    );
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(jobRepository.count()).isEqualTo(1);
    }
}