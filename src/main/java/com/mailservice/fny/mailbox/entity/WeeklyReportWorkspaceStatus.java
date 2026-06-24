package com.mailservice.fny.mailbox.entity;

public final class WeeklyReportWorkspaceStatus {

    public static final String NONE = "NONE";
    public static final String DRAFT = "DRAFT";
    public static final String SAVED = "SAVED";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String RESET = "RESET";

    private WeeklyReportWorkspaceStatus() {
    }

    public static String normalizeSaveStatus(String saveStatus) {
        if (SAVED.equalsIgnoreCase(saveStatus)) {
            return SAVED;
        }
        return DRAFT;
    }

    public static String normalizeStatusUpdate(String saveStatus) {
        if (ARCHIVED.equalsIgnoreCase(saveStatus) || RESET.equalsIgnoreCase(saveStatus)) {
            return ARCHIVED;
        }
        return normalizeSaveStatus(saveStatus);
    }

    public static boolean isArchived(String saveStatus) {
        return ARCHIVED.equals(saveStatus);
    }

    public static boolean isActive(String saveStatus) {
        return !isArchived(saveStatus);
    }
}
