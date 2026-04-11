package com.mailservice.fny.mailbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
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
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected EmailMessage() {
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
}
