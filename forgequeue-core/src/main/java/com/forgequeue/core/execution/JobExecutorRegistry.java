package com.forgequeue.core.execution;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JobExecutorRegistry {

    private final Map<String, JobExecutor> executorMap = new HashMap<>();

    public JobExecutorRegistry(List<JobExecutor> executors) {
        for (JobExecutor executor : executors) {
            executorMap.put(executor.getType(), executor);
        }
    }

    public JobExecutor getExecutor(String type) {
        return executorMap.get(type);
    }
}