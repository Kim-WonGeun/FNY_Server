package com.mailservice.fny.analysis.dto;

public record AnalysisJobCreateResponse(
        String jobId,
        String status,
        String message
) {
}
