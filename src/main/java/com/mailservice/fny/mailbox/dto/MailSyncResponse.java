package com.mailservice.fny.mailbox.dto;

import java.time.LocalDateTime;

public record MailSyncResponse(
        String mailAccountId,
        int requestedCount,
        int fetchedCount,
        int insertedCount,
        int skippedCount,
        int analysisRequestedCount,
        int analysisCompletedCount,
        int analysisSkippedCount,
        LocalDateTime syncedAt
) {
}
