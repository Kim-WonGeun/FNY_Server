package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspaceStatus;
import java.time.format.DateTimeFormatter;

public record WeeklyReportListItem(
        String reportId,
        String reportType,
        String periodStart,
        String periodEnd,
        int emailCount,
        String createdAt,
        String workspaceStatus
) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static WeeklyReportListItem from(WeeklyMailReport entity) {
        return new WeeklyReportListItem(
                entity.getId(),
                entity.getReportType(),
                ISO.format(entity.getPeriodStart()),
                ISO.format(entity.getPeriodEnd()),
                entity.getEmailCount(),
                ISO.format(entity.getCreatedAt()),
                WeeklyReportWorkspaceStatus.NONE
        );
    }

    public static WeeklyReportListItem from(WeeklyMailReport entity, String workspaceStatus) {
        return new WeeklyReportListItem(
                entity.getId(),
                entity.getReportType(),
                ISO.format(entity.getPeriodStart()),
                ISO.format(entity.getPeriodEnd()),
                entity.getEmailCount(),
                ISO.format(entity.getCreatedAt()),
                workspaceStatus
        );
    }
}
