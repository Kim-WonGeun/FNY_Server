package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.analysis.dto.AnalysisJobResponse;
import com.mailservice.fny.analysis.dto.EmailActionItemResponse;
import com.mailservice.fny.analysis.dto.EmailAnalysisResponse;
import java.time.LocalDateTime;
import java.util.List;

public record EmailDetailResponse(
        String id,
        String mailAccountId,
        String accountEmail,
        String provider,
        String externalMessageId,
        String externalThreadId,
        String internetMessageId,
        String subject,
        String bodyText,
        String bodyHtml,
        String snippet,
        String fromName,
        String fromEmail,
        LocalDateTime receivedAt,
        LocalDateTime sentAt,
        boolean read,
        boolean starred,
        boolean hasAttachment,
        String importanceHeader,
        boolean analysisEligible,
        Integer analysisCandidateScore,
        String analysisCandidateReasons,
        String analysisSkippedReason,
        LocalDateTime analysisCandidateEvaluatedAt,
        boolean attentionResolved,
        LocalDateTime attentionResolvedAt,
        String attentionStatus,
        LocalDateTime attentionStatusUpdatedAt,
        EmailAnalysisResponse analysis,
        List<EmailRecipientResponse> recipients,
        List<EmailLabelResponse> labels,
        List<EmailActionItemResponse> actionItems,
        List<AnalysisJobResponse> analysisJobs
) {
}
