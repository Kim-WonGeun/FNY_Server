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
        name = "emails",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_emails_mail_account_external_message",
                columnNames = {"mail_account_id", "external_message_id"}
        )
)
public class EmailMessage {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_account_id", nullable = false)
    private MailAccount mailAccount;

    @Column(nullable = false, length = 255)
    private String externalMessageId;

    @Column(length = 255)
    private String externalThreadId;

    @Column(length = 500)
    private String internetMessageId;

    @Column(length = 500)
    private String subject;

    @Column(columnDefinition = "CLOB")
    private String bodyText;

    @Column(columnDefinition = "CLOB")
    private String bodyHtml;

    @Column(length = 1000)
    private String messageSnippet;

    @Column(length = 255)
    private String fromName;

    @Column(nullable = false, length = 255)
    private String fromEmail;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private boolean isStarred;

    @Column(nullable = false)
    private boolean hasAttachment;

    @Column(length = 50)
    private String importanceHeader;

    @Column(columnDefinition = "CLOB")
    private String rawPayload;

    @Column(nullable = false)
    private boolean analysisEligible;

    private Integer analysisCandidateScore;

    @Column(length = 1000)
    private String analysisCandidateReasons;

    @Column(length = 100)
    private String analysisSkippedReason;

    private LocalDateTime analysisCandidateEvaluatedAt;

    @Column(nullable = false)
    private boolean attentionResolved;

    private LocalDateTime attentionResolvedAt;

    @Column(nullable = false, length = 30)
    private String attentionStatus = ATTENTION_STATUS_NEEDS_ATTENTION;

    private LocalDateTime attentionStatusUpdatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmailMessage() {
    }

    public static final String ATTENTION_STATUS_NEEDS_ATTENTION = "NEEDS_ATTENTION";
    public static final String ATTENTION_STATUS_REVIEWED = "REVIEWED";
    public static final String ATTENTION_STATUS_COMPLETED = "COMPLETED";
    public static final String ATTENTION_STATUS_DEFERRED = "DEFERRED";

    public EmailMessage(
            String id,
            MailAccount mailAccount,
            String externalMessageId,
            String externalThreadId,
            String internetMessageId,
            String subject,
            String bodyText,
            String bodyHtml,
            String messageSnippet,
            String fromName,
            String fromEmail,
            LocalDateTime receivedAt,
            LocalDateTime sentAt,
            boolean isRead,
            boolean isStarred,
            boolean hasAttachment,
            String importanceHeader,
            String rawPayload
    ) {
        this.id = id;
        this.mailAccount = mailAccount;
        this.externalMessageId = externalMessageId;
        this.externalThreadId = externalThreadId;
        this.internetMessageId = internetMessageId;
        this.subject = subject;
        this.bodyText = bodyText;
        this.bodyHtml = bodyHtml;
        this.messageSnippet = messageSnippet;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
        this.receivedAt = receivedAt;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.isStarred = isStarred;
        this.hasAttachment = hasAttachment;
        this.importanceHeader = importanceHeader;
        this.rawPayload = rawPayload;
    }

    public void markAnalysisCandidate(
            boolean eligible,
            Integer candidateScore,
            String candidateReasons,
            String skippedReason,
            LocalDateTime evaluatedAt
    ) {
        this.analysisEligible = eligible;
        this.analysisCandidateScore = candidateScore;
        this.analysisCandidateReasons = candidateReasons;
        this.analysisSkippedReason = skippedReason;
        this.analysisCandidateEvaluatedAt = evaluatedAt;
    }

    public void updateAttentionResolved(boolean resolved) {
        updateAttentionStatus(resolved ? ATTENTION_STATUS_COMPLETED : ATTENTION_STATUS_NEEDS_ATTENTION);
    }

    public void updateAttentionStatus(String status) {
        String normalized = normalizeAttentionStatus(status);
        LocalDateTime now = LocalDateTime.now();
        this.attentionStatus = normalized;
        this.attentionStatusUpdatedAt = ATTENTION_STATUS_NEEDS_ATTENTION.equals(normalized) ? null : now;
        this.attentionResolved = isClosedAttentionStatus(normalized);
        this.attentionResolvedAt = this.attentionResolved ? now : null;
    }

    public static boolean isClosedAttentionStatus(String status) {
        return !ATTENTION_STATUS_NEEDS_ATTENTION.equals(normalizeAttentionStatus(status));
    }

    private static String normalizeAttentionStatus(String status) {
        if (status == null || status.isBlank()) {
            return ATTENTION_STATUS_NEEDS_ATTENTION;
        }
        return switch (status.trim().toUpperCase()) {
            case ATTENTION_STATUS_REVIEWED -> ATTENTION_STATUS_REVIEWED;
            case ATTENTION_STATUS_COMPLETED -> ATTENTION_STATUS_COMPLETED;
            case ATTENTION_STATUS_DEFERRED -> ATTENTION_STATUS_DEFERRED;
            default -> ATTENTION_STATUS_NEEDS_ATTENTION;
        };
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

    public MailAccount getMailAccount() {
        return mailAccount;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public String getExternalThreadId() {
        return externalThreadId;
    }

    public String getInternetMessageId() {
        return internetMessageId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBodyText() {
        return bodyText;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public String getMessageSnippet() {
        return messageSnippet;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public boolean isHasAttachment() {
        return hasAttachment;
    }

    public String getImportanceHeader() {
        return importanceHeader;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public boolean isAnalysisEligible() {
        return analysisEligible;
    }

    public Integer getAnalysisCandidateScore() {
        return analysisCandidateScore;
    }

    public String getAnalysisCandidateReasons() {
        return analysisCandidateReasons;
    }

    public String getAnalysisSkippedReason() {
        return analysisSkippedReason;
    }

    public LocalDateTime getAnalysisCandidateEvaluatedAt() {
        return analysisCandidateEvaluatedAt;
    }

    public boolean isAttentionResolved() {
        return attentionResolved;
    }

    public LocalDateTime getAttentionResolvedAt() {
        return attentionResolvedAt;
    }

    public String getAttentionStatus() {
        return attentionStatus;
    }

    public LocalDateTime getAttentionStatusUpdatedAt() {
        return attentionStatusUpdatedAt;
    }
}
