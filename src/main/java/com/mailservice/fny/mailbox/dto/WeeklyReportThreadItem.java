package com.mailservice.fny.mailbox.dto;

public record WeeklyReportThreadItem(
        String emailId,
        String subject,
        String oneLiner,
        String fromEmail,
        String receivedAt,
        String reportSection,
        String evidenceText
) {
}
