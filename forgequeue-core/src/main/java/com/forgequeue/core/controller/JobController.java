package com.forgequeue.core.controller;

import com.forgequeue.core.domain.Job;
import com.forgequeue.core.dto.JobSubmitRequest;
import com.forgequeue.core.dto.JobSubmitResponse;
import com.forgequeue.core.service.JobSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobSubmissionService submissionService;

    public JobController(JobSubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<JobSubmitResponse> submit(
            @RequestBody JobSubmitRequest request) {

        Job job = submissionService.submitJob(
                request.getUserId(),
                request.getIdempotencyKey(),
                request.getType(),
                request.getPriority(),
                request.getPayload()
        );

        return ResponseEntity.ok(JobSubmitResponse.from(job));
    }
}