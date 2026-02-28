package com.forgequeue.core;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.repository.JobRepository;
import com.forgequeue.core.service.JobSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JobSubmissionIT extends AbstractIntegrationTest {

    @Autowired
    private JobSubmissionService submissionService;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void shouldReturnSameJobForSameIdempotencyKey() {

        String userId = "user-1";
        String key = "idem-123";

        Job first = submissionService.submitJob(
                userId,
                key,
                "EMAIL",
                5,
                Map.of("to", "a@b.com")
        );

        Job second = submissionService.submitJob(
                userId,
                key,
                "EMAIL",
                5,
                Map.of("to", "a@b.com")
        );

        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(jobRepository.count()).isEqualTo(1);
    }
}