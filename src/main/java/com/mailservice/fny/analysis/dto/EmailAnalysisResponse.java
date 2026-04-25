package com.mailservice.fny.analysis.dto;

import com.mailservice.fny.analysis.entity.EmailAnalysis;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public record EmailAnalysisResponse(
        String id,
        int analysisVersion,
        String modelName,
        String promptVersion,
        String shortSummary,
        String detailedSummary,
        String category,
        String priorityLevel,
        BigDecimal importanceScore,
        BigDecimal urgencyScore,
        BigDecimal confidenceScore,
        Boolean needsReply,
        Boolean hasDeadline,
        LocalDateTime deadlineAt,
        String deadlineText,
        String timeSensitivity,
        Boolean requiresAction,
        String userTaskSummary,
        List<String> priorityReasonCodes,
        String suggestedAction,
        String reasoning,
        String status,
        LocalDateTime analyzedAt
) {

    public static EmailAnalysisResponse from(EmailAnalysis analysis) {
        return new EmailAnalysisResponse(
                analysis.getId(),
                analysis.getAnalysisVersion(),
                analysis.getModelName(),
                analysis.getPromptVersion(),
                analysis.getShortSummary(),
                analysis.getDetailedSummary(),
                analysis.getCategory(),
                analysis.getPriorityLevel(),
                analysis.getImportanceScore(),
                analysis.getUrgencyScore(),
                analysis.getConfidenceScore(),
                analysis.getNeedsReply(),
                analysis.getHasDeadline(),
                analysis.getDeadlineAt(),
                analysis.getDeadlineText(),
                analysis.getTimeSensitivity(),
                analysis.getRequiresAction(),
                analysis.getUserTaskSummary(),
                splitCodes(analysis.getPriorityReasonCodes()),
                analysis.getSuggestedAction(),
                analysis.getReasoning(),
                analysis.getStatus(),
                analysis.getAnalyzedAt()
        );
    }

    private static List<String> splitCodes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::strip)
                .filter(code -> !code.isBlank())
                .toList();
    }
}
