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
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_mail_reports")
public class WeeklyMailReport {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_account_id", nullable = false)
    private MailAccount mailAccount;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(nullable = false)
    private int emailCount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 30)
    private String reportType;

    @Column(length = 2000)
    private String executiveSummary;

    @Column(nullable = false, columnDefinition = "CLOB")
    private String reportPayload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected WeeklyMailReport() {
    }

    public WeeklyMailReport(
            String id,
            MailAccount mailAccount,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            int emailCount,
            String status,
            String reportType,
            String executiveSummary,
            String reportPayload
    ) {
        this.id = id;
        this.mailAccount = mailAccount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.emailCount = emailCount;
        this.status = status;
        this.reportType = reportType;
        this.executiveSummary = executiveSummary;
        this.reportPayload = reportPayload;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null || updatedAt == null) {
            LocalDateTime now = LocalDateTime.now();
            createdAt = now;
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public MailAccount getMailAccount() {
        return mailAccount;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public int getEmailCount() {
        return emailCount;
    }

    public String getStatus() {
        return status;
    }

    public String getReportType() {
        return reportType;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public String getReportPayload() {
        return reportPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
