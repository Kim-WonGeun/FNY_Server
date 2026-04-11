package com.mailservice.fny.analysis.entity;

import com.mailservice.fny.mailbox.entity.EmailMessage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    public String getId() {
        return id;
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
