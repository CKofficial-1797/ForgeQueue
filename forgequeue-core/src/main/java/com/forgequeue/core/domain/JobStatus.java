package com.forgequeue.core.domain;

public enum JobStatus {

    CREATED,
    QUEUED,
    LEASED,
    PROCESSING,
    RETRY_WAIT,
    COMPLETED,
    FAILED,
    DEAD_LETTER,
    CANCELLED
}