package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerScheduler {

    private final WorkerProperties properties;
    private final JobLeasingService leasingService;
    private final JobExecutionService executionService;

    public WorkerScheduler(WorkerProperties properties,
                           JobLeasingService leasingService,
                           JobExecutionService executionService) {
        this.properties = properties;
        this.leasingService = leasingService;
        this.executionService = executionService;
    }

    @Scheduled(fixedDelayString = "${forgequeue.worker.poll-interval-ms}")
    public void poll() {

        if (!properties.isEnabled()) {
            return;
        }

        List<Job> jobs = leasingService.leaseBatch();

        if (jobs.isEmpty()) {
            return;
        }

        for (Job job : jobs) {
            process(job);
        }
    }

    private void process(Job job) {
        executionService.execute(job);
    }
}