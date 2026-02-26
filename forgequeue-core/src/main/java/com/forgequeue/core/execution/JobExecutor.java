package com.forgequeue.core.execution;

import com.forgequeue.core.domain.Job;

public interface JobExecutor {

    String getType();

    void execute(Job job) throws Exception;
}