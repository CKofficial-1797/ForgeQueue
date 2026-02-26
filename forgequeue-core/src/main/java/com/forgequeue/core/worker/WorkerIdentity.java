package com.forgequeue.core.worker;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkerIdentity {

    private String workerId;

    @PostConstruct
    public void init() {
        this.workerId = UUID.randomUUID().toString();
    }

    public String getWorkerId() {
        return workerId;
    }
}