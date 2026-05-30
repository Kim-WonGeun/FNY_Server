package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.dto.AnalysisJobResponse;
import com.mailservice.fny.analysis.dto.EmailActionItemResponse;
import com.mailservice.fny.analysis.dto.EmailAnalysisResponse;
import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.dto.EmailLabelResponse;
import com.mailservice.fny.mailbox.dto.EmailRecipientResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmailDetailResponseMapper {

    EmailDetailResponse toResponse(
            EmailMessage email,
            EmailAnalysisResponse analysis,
            List<EmailRecipientResponse> recipients,
            List<EmailLabelResponse> labels,
            List<EmailActionItemResponse> actionItems,
            List<AnalysisJobResponse> analysisJobs
    ) {
        return new EmailDetailResponse(
                email.getId(),
                email.getMailAccount().getId(),
                email.getMailAccount().getAccountEmail(),
                email.getMailAccount().getProvider(),
                email.getExternalMessageId(),
                email.getExternalThreadId(),
                email.getInternetMessageId(),
                email.getSubject(),
                email.getBodyText(),
                email.getBodyHtml(),
                email.getMessageSnippet(),
                email.getFromName(),
                email.getFromEmail(),
                email.getReceivedAt(),
                email.getSentAt(),
                email.isRead(),
                email.isStarred(),
                email.isHasAttachment(),
                email.getImportanceHeader(),
                email.isAnalysisEligible(),
                email.getAnalysisCandidateScore(),
                email.getAnalysisCandidateReasons(),
                email.getAnalysisSkippedReason(),
                email.getAnalysisCandidateEvaluatedAt(),
                email.isAttentionResolved(),
                email.getAttentionResolvedAt(),
                email.getAttentionStatus(),
                email.getAttentionStatusUpdatedAt(),
                analysis,
                recipients,
                labels,
                actionItems,
                analysisJobs
        );
    }
}
