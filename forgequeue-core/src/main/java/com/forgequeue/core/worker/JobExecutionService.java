package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.execution.JobExecutor;
import com.forgequeue.core.execution.JobExecutorRegistry;
import com.forgequeue.core.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
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

            String result = executor.execute(job);

            markCompleted(job, result);

        } catch (Exception ex) {

            handleFailure(job, ex);
        }
    }

    @Transactional
    protected void markCompleted(Job job, String resultPayload) {

        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        job.setLeaseExpiresAt(null);
        job.setWorkerId(null);
        job.setResultPayload(resultPayload);
        job.setLastErrorMessage(null);
        job.setLastErrorStacktrace(null);

        jobRepository.save(job);
    }

    @Transactional
    protected void handleFailure(Job job, Exception ex) {

        String errorMessage = ex.getMessage();
        String stackTrace = getStackTrace(ex);

        job.setLastErrorMessage(errorMessage);
        job.setLastErrorStacktrace(stackTrace);

        if (job.getAttemptCount() >= job.getMaxAttempts()) {

            job.setStatus(JobStatus.DEAD_LETTER);
            job.setFailedAt(Instant.now());
            job.setLeaseExpiresAt(null);
            job.setWorkerId(null);

            jobRepository.save(job);
            return;
        }

        long base = properties.getRetryBaseDelaySeconds();
        long maxDelay = properties.getRetryMaxDelaySeconds();
        double jitterFactor = properties.getRetryJitterFactor();

        long rawDelay = base * (1L << (job.getAttemptCount() - 1));

        // Cap delay
        long cappedDelay = Math.min(rawDelay, maxDelay);

        // jitter (± jitterFactor)
        long jitterRange = (long) (cappedDelay * jitterFactor);
        long randomJitter = (long) ((Math.random() * (2 * jitterRange)) - jitterRange);

        long finalDelay = cappedDelay + randomJitter;

        // Safety floor
        if (finalDelay < 1) {
            finalDelay = 1;
        }

        Instant nextRun = Instant.now().plusSeconds(finalDelay);

        job.setStatus(JobStatus.QUEUED);
        job.setNextRunAt(nextRun);
        job.setLeaseExpiresAt(null);
        job.setWorkerId(null);

        jobRepository.save(job);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

