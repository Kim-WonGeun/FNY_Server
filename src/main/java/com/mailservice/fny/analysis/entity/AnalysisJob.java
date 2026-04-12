package com.mailservice.fny.analysis.entity;

import com.mailservice.fny.mailbox.entity.EmailMessage;

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
@Table(name = "analysis_jobs")
public class AnalysisJob {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    private EmailMessage email;

    @Column(nullable = false, length = 50)
    private String jobType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private int maxRetries;

    @Column(length = 100)
    private String workerId;

    @Column(columnDefinition = "CLOB")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected AnalysisJob() {
    }

    public AnalysisJob(String id, EmailMessage email, String jobType, String status, int priority) {
        this.id = id;
        this.email = email;
        this.jobType = jobType;
        this.status = status;
        this.priority = priority;
        this.retryCount = 0;
        this.maxRetries = 3;
    }

    public void complete(String workerId) {
        this.status = "COMPLETED";
        this.workerId = workerId;
        this.errorMessage = null;
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        this.completedAt = LocalDateTime.now();
    }

    public EmailMessage getEmail() {
        return email;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getJobType() {
        return jobType;
    }

    public String getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
