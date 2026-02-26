package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.execution.JobExecutor;
import com.forgequeue.core.execution.JobExecutorRegistry;
import com.forgequeue.core.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JobExecutionService {

    private final JobExecutorRegistry registry;
    private final JobRepository jobRepository;
    private final WorkerProperties properties;

    public JobExecutionService(JobExecutorRegistry registry,
                               JobRepository jobRepository,
                               WorkerProperties properties) {
        this.registry = registry;
        this.jobRepository = jobRepository;
        this.properties = properties;
    }

    public void execute(Job job) {

        try {
            JobExecutor executor = registry.getExecutor(job.getType());

            if (executor == null) {
                throw new IllegalStateException("No executor found for type: " + job.getType());
            }

            executor.execute(job);

            markCompleted(job);

        } catch (Exception ex) {

            System.out.println("Execution failed for job " + job.getId() + ": " + ex.getMessage());

            handleFailure(job);
        }
    }

    @Transactional
    protected void markCompleted(Job job) {

        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        job.setLeaseExpiresAt(null);
        job.setWorkerId(null);

        jobRepository.save(job);
    }

    @Transactional
    protected void handleFailure(Job job) {

        if (job.getAttemptCount() >= job.getMaxAttempts()) {

            job.setStatus(JobStatus.DEAD_LETTER);
            job.setFailedAt(Instant.now());
            job.setLeaseExpiresAt(null);
            job.setWorkerId(null);

            jobRepository.save(job);
            return;
        }

        long base = properties.getRetryBaseDelaySeconds();
        long delaySeconds = base * (1L << (job.getAttemptCount() - 1));

        Instant nextRun = Instant.now().plusSeconds(delaySeconds);

        job.setStatus(JobStatus.QUEUED);
        job.setNextRunAt(nextRun);
        job.setLeaseExpiresAt(null);
        job.setWorkerId(null);

        jobRepository.save(job);
    }
}