package com.mailservice.fny.mailbox.entity;

public final class WeeklyMailReportType {

    public static final String WEEKLY = "WEEKLY";
    public static final String PROGRESS = "PROGRESS";
    public static final String ISSUE = "ISSUE";

    private WeeklyMailReportType() {
    }

    public static String normalize(String reportType) {
        if (reportType == null || reportType.isBlank()) {
            return WEEKLY;
        }
        return switch (reportType.trim().toUpperCase()) {
            case PROGRESS -> PROGRESS;
            case ISSUE -> ISSUE;
            default -> WEEKLY;
        };
    }
}
