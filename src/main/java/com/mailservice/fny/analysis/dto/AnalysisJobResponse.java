package com.mailservice.fny.analysis.dto;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import java.time.LocalDateTime;

public record AnalysisJobResponse(
        String id,
        String jobType,
        String status,
        int priority,
        int retryCount,
        int maxRetries,
        String workerId,
        String errorMessage,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {

    public static AnalysisJobResponse from(AnalysisJob job) {
        return new AnalysisJobResponse(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getPriority(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getWorkerId(),
                job.getErrorMessage(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getCreatedAt()
        );
    }
}
