package com.mailservice.fny.mailbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "weekly_report_workspaces",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_weekly_report_workspaces_report_user",
                columnNames = {"report_id", "user_id"}
        )
)
public class WeeklyReportWorkspace {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private WeeklyMailReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String draftText;

    @Column(nullable = false, length = 20)
    private String saveStatus;

    @Column(columnDefinition = "CLOB")
    private String excludedSourceIds;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected WeeklyReportWorkspace() {
    }

    public WeeklyReportWorkspace(
            String id,
            WeeklyMailReport report,
            AppUser user,
            String draftText,
            String saveStatus,
            String excludedSourceIds
    ) {
        this.id = id;
        this.report = report;
        this.user = user;
        update(draftText, saveStatus, excludedSourceIds);
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (savedAt == null) {
            savedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(String draftText, String saveStatus, String excludedSourceIds) {
        this.draftText = draftText;
        this.saveStatus = saveStatus;
        this.excludedSourceIds = excludedSourceIds;
        this.savedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public WeeklyMailReport getReport() {
        return report;
    }

    public AppUser getUser() {
        return user;
    }

    public String getDraftText() {
        return draftText;
    }

    public String getSaveStatus() {
        return saveStatus;
    }

    public String getExcludedSourceIds() {
        return excludedSourceIds;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
}
