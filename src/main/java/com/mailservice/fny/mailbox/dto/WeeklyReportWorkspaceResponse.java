package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record WeeklyReportWorkspaceResponse(
        String reportId,
        String userId,
        String draftText,
        String saveStatus,
        List<String> excludedSourceIds,
        String savedAt
) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static WeeklyReportWorkspaceResponse from(
            WeeklyReportWorkspace workspace,
            List<String> excludedSourceIds
    ) {
        return new WeeklyReportWorkspaceResponse(
                workspace.getReport().getId(),
                workspace.getUser().getId(),
                workspace.getDraftText(),
                workspace.getSaveStatus(),
                excludedSourceIds,
                ISO.format(workspace.getSavedAt())
        );
    }
}
