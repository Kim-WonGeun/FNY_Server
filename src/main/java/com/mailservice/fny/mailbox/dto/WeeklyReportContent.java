package com.mailservice.fny.mailbox.dto;

import java.util.List;

public record WeeklyReportContent(
        String source,
        String executiveSummary,
        List<String> highlights,
        List<String> risksBlockers,
        List<String> pendingDecisions,
        List<String> nextWeekSuggestions,
        List<WeeklyReportThreadItem> threadSummaries,
        String modelName,
        String promptVersion
) {
}
