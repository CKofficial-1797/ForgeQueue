package com.forgequeue.core.execution;

import com.forgequeue.core.domain.Job;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailJobExecutor implements JobExecutor {

    @Override
    public String getType() {
        return "email";
    }

    @Override
public Map<String, Object> execute(Job job) throws Exception {

    if (job.getPayload() == null) {
        throw new IllegalArgumentException("Email payload cannot be null");
    }

    Map<String, Object> result = new HashMap<>();
    result.put("status", "sent");
    result.put("timestamp", System.currentTimeMillis());

    return result;
}
}