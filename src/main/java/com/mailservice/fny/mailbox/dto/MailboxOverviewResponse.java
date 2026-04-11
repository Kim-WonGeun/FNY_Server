package com.mailservice.fny.mailbox.dto;

import java.util.List;

public record MailboxOverviewResponse(
        String userId,
        long totalEmails,
        long unreadEmails,
        long needsReplyEmails,
        long highPriorityEmails,
        long pendingAnalysisJobs,
        List<EmailListResponse> spotlightEmails
) {
}
