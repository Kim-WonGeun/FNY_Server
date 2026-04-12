package com.mailservice.fny.analysis.dto;

public record AnalysisResultSaveResponse(
        String jobId,
        String emailId,
        String analysisId,
        String status,
        int actionItemCount
) {
}
