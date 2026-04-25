package com.mailservice.fny.mailbox.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InboxEmailSummary(
        String id,
        String subject,
        String messageSnippet,
        String fromName,
        String fromEmail,
        LocalDateTime receivedAt,
        boolean isRead,
        boolean isStarred,
        boolean hasAttachment,
        String category,
        String priorityLevel,
        BigDecimal importanceScore,
        BigDecimal urgencyScore,
        String shortSummary,
        Boolean needsReply,
        boolean analysisEligible,
        Integer analysisCandidateScore,
        String analysisCandidateReasons,
        String analysisSkippedReason,
        LocalDateTime analysisCandidateEvaluatedAt,
        boolean attentionResolved,
        LocalDateTime attentionResolvedAt,
        String attentionStatus,
        LocalDateTime attentionStatusUpdatedAt
) {
}
