package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.domain.JobStatus;
import com.forgequeue.core.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JobLeasingService {

    private final JobRepository jobRepository;
    private final WorkerIdentity workerIdentity;
    private final WorkerProperties properties;

    public JobLeasingService(JobRepository jobRepository,
                             WorkerIdentity workerIdentity,
                             WorkerProperties properties) {
        this.jobRepository = jobRepository;
        this.workerIdentity = workerIdentity;
        this.properties = properties;
    }

    @Transactional
    public List<Job> leaseBatch() {

        List<Job> jobs = jobRepository.leaseCandidates(properties.getBatchSize());

        if (jobs.isEmpty()) {
            return jobs;
        }

        Instant now = Instant.now();
        Instant leaseExpiry = now.plusSeconds(properties.getVisibilityTimeoutSeconds());

        for (Job job : jobs) {
            job.setStatus(JobStatus.PROCESSING);
            job.setWorkerId(workerIdentity.getWorkerId());
            job.setLeaseExpiresAt(leaseExpiry);
            job.setAttemptCount(job.getAttemptCount() + 1);
        }
        jobRepository.saveAll(jobs); 
        return jobs;
    }
}