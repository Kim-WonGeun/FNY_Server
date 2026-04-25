package com.mailservice.fny.mailbox.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record EmailListResponse(
        String id,
        String subject,
        String snippet,
        String fromName,
        String fromEmail,
        LocalDateTime receivedAt,
        boolean read,
        boolean starred,
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
        LocalDateTime attentionStatusUpdatedAt,
        List<String> attentionReasons
) {

    public static EmailListResponse from(InboxEmailSummary summary) {
        return new EmailListResponse(
                summary.id(),
                summary.subject(),
                summary.messageSnippet(),
                summary.fromName(),
                summary.fromEmail(),
                summary.receivedAt(),
                summary.isRead(),
                summary.isStarred(),
                summary.hasAttachment(),
                summary.category(),
                summary.priorityLevel(),
                summary.importanceScore(),
                summary.urgencyScore(),
                summary.shortSummary(),
                summary.needsReply(),
                summary.analysisEligible(),
                summary.analysisCandidateScore(),
                summary.analysisCandidateReasons(),
                summary.analysisSkippedReason(),
                summary.analysisCandidateEvaluatedAt(),
                summary.attentionResolved(),
                summary.attentionResolvedAt(),
                summary.attentionStatus(),
                summary.attentionStatusUpdatedAt(),
                resolveAttentionReasons(summary)
        );
    }

    private static List<String> resolveAttentionReasons(InboxEmailSummary summary) {
        List<String> reasons = new ArrayList<>();

        if (summary.attentionResolved()) {
            return reasons;
        }

        if ("P1".equals(summary.priorityLevel()) || "P2".equals(summary.priorityLevel())) {
            reasons.add("HIGH_PRIORITY");
        }

        if (Boolean.TRUE.equals(summary.needsReply())) {
            reasons.add("NEEDS_REPLY");
        }

        if (!summary.isRead()) {
            reasons.add("UNREAD");
        }

        if (summary.isStarred()) {
            reasons.add("STARRED");
        }

        return reasons;
    }
}
