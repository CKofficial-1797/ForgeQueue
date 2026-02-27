package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.execution.JobExecutor;
import com.forgequeue.core.execution.JobExecutorRegistry;
import com.forgequeue.core.repository.JobRepository;
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

    //     // TEMP DEBUG — will be remove after verification
    // System.out.println("Max concurrent per user = "
    //         + properties.getMaxConcurrentJobsPerUser());

    

        String key = "active_jobs:" + job.getUserId();
        
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(
                    key,
                    properties.getConcurrentCounterTtlSeconds(),
                    TimeUnit.SECONDS
            );
        }

        // If user exceeds allowed concurrent jobs → rollback and skip
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

        //     System.out.println(
        // "START job=" + job.getId() +
        // " user=" + job.getUserId() +
        // " time=" + Instant.now()
        //     );


            Map<String, Object> result = executor.execute(job);

            markCompleted(job, result);

        //     System.out.println(
        // "END job=" + job.getId() +
        // " user=" + job.getUserId() +
        // " time=" + Instant.now()
        // );

        } catch (Exception ex) {

            handleFailure(job, ex);

        } finally {

            Long after = redisTemplate.opsForValue().decrement(key);

            // Prevent negative counter values
            if (after != null && after <= 0) {
                redisTemplate.delete(key);
            }
        }
    }

    @Transactional
    protected void markCompleted(Job job, Map<String, Object> resultPayload) {

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