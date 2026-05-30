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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_analysis")
public class EmailAnalysis {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    private EmailMessage email;

    @Column(nullable = false)
    private int analysisVersion;

    @Column(nullable = false)
    private boolean isLatest;

    @Column(length = 100)
    private String modelName;

    @Column(length = 50)
    private String promptVersion;

    @Column(length = 1000)
    private String shortSummary;

    @Column(columnDefinition = "CLOB")
    private String detailedSummary;

    @Column(length = 50)
    private String category;

    @Column(length = 10)
    private String priorityLevel;

    @Column(precision = 5, scale = 2)
    private BigDecimal importanceScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal urgencyScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    private Boolean needsReply;

    private Boolean hasDeadline;

    private LocalDateTime deadlineAt;

    @Column(length = 255)
    private String deadlineText;

    @Column(length = 30)
    private String timeSensitivity;

    private Boolean requiresAction;

    @Column(length = 1000)
    private String userTaskSummary;

    @Column(length = 1000)
    private String priorityReasonCodes;

    @Column(length = 255)
    private String suggestedAction;

    @Column(columnDefinition = "CLOB")
    private String reasoning;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmailAnalysis() {
    }

    public EmailAnalysis(
            String id,
            EmailMessage email,
            int analysisVersion,
            String modelName,
            String promptVersion,
            String shortSummary,
            String detailedSummary,
            String category,
            String priorityLevel,
            BigDecimal importanceScore,
            BigDecimal urgencyScore,
            BigDecimal confidenceScore,
            Boolean needsReply,
            Boolean hasDeadline,
            LocalDateTime deadlineAt,
            String deadlineText,
            String timeSensitivity,
            Boolean requiresAction,
            String userTaskSummary,
            String priorityReasonCodes,
            String suggestedAction,
            String reasoning
    ) {
        this.id = id;
        this.email = email;
        this.analysisVersion = analysisVersion;
        this.isLatest = true;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        this.shortSummary = shortSummary;
        this.detailedSummary = detailedSummary;
        this.category = category;
        this.priorityLevel = priorityLevel;
        this.importanceScore = importanceScore;
        this.urgencyScore = urgencyScore;
        this.confidenceScore = confidenceScore;
        this.needsReply = needsReply;
        this.hasDeadline = hasDeadline;
        this.deadlineAt = deadlineAt;
        this.deadlineText = deadlineText;
        this.timeSensitivity = timeSensitivity;
        this.requiresAction = requiresAction;
        this.userTaskSummary = userTaskSummary;
        this.priorityReasonCodes = priorityReasonCodes;
        this.suggestedAction = suggestedAction;
        this.reasoning = reasoning;
        this.status = "COMPLETED";
        this.analyzedAt = LocalDateTime.now();
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

    public void markNotLatest() {
        this.isLatest = false;
    }

    public String getId() {
        return id;
    }

    public EmailMessage getEmail() {
        return email;
    }

    public int getAnalysisVersion() {
        return analysisVersion;
    }

    public boolean isLatest() {
        return isLatest;
    }

    public String getModelName() {
        return modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public String getShortSummary() {
        return shortSummary;
    }

    public String getDetailedSummary() {
        return detailedSummary;
    }

    public String getCategory() {
        return category;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public BigDecimal getImportanceScore() {
        return importanceScore;
    }

    public BigDecimal getUrgencyScore() {
        return urgencyScore;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public Boolean getNeedsReply() {
        return needsReply;
    }

    public Boolean getHasDeadline() {
        return hasDeadline;
    }

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public String getDeadlineText() {
        return deadlineText;
    }

    public String getTimeSensitivity() {
        return timeSensitivity;
    }

    public Boolean getRequiresAction() {
        return requiresAction;
    }

    public String getUserTaskSummary() {
        return userTaskSummary;
    }

    public String getPriorityReasonCodes() {
        return priorityReasonCodes;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public String getReasoning() {
        return reasoning;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}
