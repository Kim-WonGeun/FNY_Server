package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record WeeklyReportResponse(
        String reportId,
        String mailAccountId,
        String reportType,
        String periodStart,
        String periodEnd,
        int emailCount,
        String source,
        String executiveSummary,
        List<String> highlights,
        List<String> risksBlockers,
        List<String> pendingDecisions,
        List<String> nextWeekSuggestions,
        List<WeeklyReportThreadItem> threadSummaries,
        String modelName,
        String promptVersion,
        String createdAt
) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static WeeklyReportResponse from(WeeklyMailReport entity, WeeklyReportContent content) {
        return new WeeklyReportResponse(
                entity.getId(),
                entity.getMailAccount().getId(),
                entity.getReportType(),
                ISO.format(entity.getPeriodStart()),
                ISO.format(entity.getPeriodEnd()),
                entity.getEmailCount(),
                content.source(),
                content.executiveSummary(),
                content.highlights(),
                content.risksBlockers(),
                content.pendingDecisions(),
                content.nextWeekSuggestions(),
                content.threadSummaries(),
                content.modelName(),
                content.promptVersion(),
                ISO.format(entity.getCreatedAt())
        );
    }
}
