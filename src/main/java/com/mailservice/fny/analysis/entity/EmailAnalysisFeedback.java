package com.mailservice.fny.analysis.entity;

import com.mailservice.fny.mailbox.entity.AppUser;
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
@Table(name = "email_analysis_feedback")
public class EmailAnalysisFeedback {

    public static final String TYPE_ACCEPTED = "ACCEPTED";
    public static final String TYPE_NEEDS_FIX = "NEEDS_FIX";

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private EmailAnalysis analysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id", nullable = false)
    private EmailMessage email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 30)
    private String feedbackType;

    @Column(length = 50)
    private String reasonCode;

    @Column(length = 1000)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmailAnalysisFeedback() {
    }

    public EmailAnalysisFeedback(
            String id,
            EmailAnalysis analysis,
            EmailMessage email,
            AppUser user,
            String feedbackType,
            String reasonCode,
            String memo
    ) {
        this.id = id;
        this.analysis = analysis;
        this.email = email;
        this.user = user;
        update(feedbackType, reasonCode, memo);
    }

    public void update(String feedbackType, String reasonCode, String memo) {
        this.feedbackType = normalizeFeedbackType(feedbackType);
        this.reasonCode = normalizeNullable(reasonCode);
        this.memo = normalizeNullable(memo);
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

    public EmailAnalysis getAnalysis() {
        return analysis;
    }

    public EmailMessage getEmail() {
        return email;
    }

    public AppUser getUser() {
        return user;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private static String normalizeFeedbackType(String feedbackType) {
        if (feedbackType == null || feedbackType.isBlank()) {
            throw new IllegalArgumentException("feedbackType은 필수입니다.");
        }
        String normalized = feedbackType.trim().toUpperCase();
        if (!TYPE_ACCEPTED.equals(normalized) && !TYPE_NEEDS_FIX.equals(normalized)) {
            throw new IllegalArgumentException("지원하지 않는 feedbackType입니다. feedbackType=" + feedbackType);
        }
        return normalized;
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
