package com.forgequeue.core.service;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
// import java.util.UUID;
import java.util.Map;

@Service
public class JobSubmissionService {

    private final JobRepository jobRepository;

    public JobSubmissionService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public Job submitJob(String userId,
                        String idempotencyKey,
                        String type,
                        Integer priority,
                        Map<String, Object> payload) {

        Optional<Job> existing =
                jobRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);

        if (existing.isPresent()) {
            return existing.get();
        }

        Job job = new Job();
        job.setUserId(userId);
        job.setIdempotencyKey(idempotencyKey);
        job.setType(type);
        job.setStatus(JobStatus.QUEUED);
        job.setPriority(priority != null ? priority : 10);
        job.setPayload(payload);
        job.setAttemptCount(0);
        job.setMaxAttempts(3);
        job.setNextRunAt(Instant.now());

        return jobRepository.save(job);
    }
}