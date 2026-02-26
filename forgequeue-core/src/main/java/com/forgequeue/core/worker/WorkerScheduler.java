package com.forgequeue.core.worker;

import com.forgequeue.core.domain.Job;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerScheduler {

    private final WorkerProperties properties;
    private final JobLeasingService leasingService;

    public WorkerScheduler(WorkerProperties properties,
                           JobLeasingService leasingService) {
        this.properties = properties;
        this.leasingService = leasingService;
    }

    @Scheduled(fixedDelayString = "${forgequeue.worker.poll-interval-ms}")
    public void poll() {

        if (!properties.isEnabled()) {
            return;
        }

        List<Job> jobs = leasingService.leaseBatch();

        if (!jobs.isEmpty()) {
            System.out.println("Leased " + jobs.size() + " jobs");
        }
    }
}