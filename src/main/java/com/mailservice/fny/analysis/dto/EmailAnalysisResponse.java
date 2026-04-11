package com.mailservice.fny.analysis.dto;

import com.mailservice.fny.analysis.entity.EmailAnalysis;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                analysis.getSuggestedAction(),
                analysis.getReasoning(),
                analysis.getStatus(),
                analysis.getAnalyzedAt()
        );
    }
}
