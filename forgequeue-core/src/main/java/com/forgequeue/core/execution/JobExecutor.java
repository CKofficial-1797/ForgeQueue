package com.forgequeue.core.execution;

import com.forgequeue.core.domain.Job;
import java.util.Map;

public interface JobExecutor {

    // Simple string type identifier
    String getType();

    // Execute job and return result JSON
    Map<String, Object> execute(Job job) throws Exception;
}