package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.execution.JobExecutor;
import com.forgequeue.core.execution.JobExecutorRegistry;
import com.forgequeue.core.repository.JobRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JobExecutionService {

    private final JobExecutorRegistry registry;
    private final JobRepository jobRepository;
    private final WorkerProperties properties;
    private final StringRedisTemplate redisTemplate;

    public JobExecutionService(JobExecutorRegistry registry,
                               JobRepository jobRepository,
                               WorkerProperties properties,
                               StringRedisTemplate redisTemplate) {
        this.registry = registry;
        this.jobRepository = jobRepository;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    public void execute(Job job) {

        String key = "active_jobs:" + job.getUserId();

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(
                    key,
                    properties.getConcurrentCounterTtlSeconds(),
                    TimeUnit.SECONDS
            );
        }

        // Enforce per-user concurrency limit
        if (count != null &&
            count > properties.getMaxConcurrentJobsPerUser()) {

            redisTemplate.opsForValue().decrement(key);
            return;
        }

        try {

            JobExecutor executor = registry.getExecutor(job.getType());

            if (executor == null) {
                throw new IllegalStateException(
                        "No executor found for type: " + job.getType());
            }

            //  Protected call
            Map<String, Object> result =
                    executeWithResilience(executor, job);

            markCompleted(job, result);

        } catch (Exception ex) {

            handleFailure(job, ex);

        } finally {

            Long after = redisTemplate.opsForValue().decrement(key);

            if (after != null && after <= 0) {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * Only the external execution is protected by Resilience4j.
     * Redis logic and failure scheduling must NOT be retried.
     */
    @CircuitBreaker(name = "jobExecution")
    @Retry(name = "jobExecution")
    protected Map<String, Object> executeWithResilience(
            JobExecutor executor,
            Job job) throws Exception {

        return executor.execute(job);
    }

    @Transactional
    protected void markCompleted(Job job,
                                 Map<String, Object> resultPayload) {

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
        long cappedDelay = Math.min(rawDelay, maxDelay);

        long jitterRange = (long) (cappedDelay * jitterFactor);
        long randomJitter =
                (long) ((Math.random() * (2 * jitterRange)) - jitterRange);

        long finalDelay = cappedDelay + randomJitter;

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