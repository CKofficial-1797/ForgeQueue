package com.forgequeue.core.execution;

import com.forgequeue.core.domain.Job;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EmailJobExecutor implements JobExecutor {

    private final Random random = new Random();

    @Override
    public String getType() {
        return "email";
    }

    @Override
    public void execute(Job job) throws Exception {

        // Simulate processing time
        Thread.sleep(500);

        // Simulate random failure (30%)
        if (random.nextInt(10) < 3) {
            throw new RuntimeException("Simulated email sending failure");
        }

        System.out.println("Email job executed successfully: " + job.getId());
    }
}