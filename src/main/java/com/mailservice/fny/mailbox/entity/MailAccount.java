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
        name = "mail_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mail_accounts_provider_provider_account",
                        columnNames = {"provider", "provider_account_id"}
                ),
                @UniqueConstraint(
                        name = "uk_mail_accounts_provider_account_email",
                        columnNames = {"provider", "account_email"}
                )
        }
)
public class MailAccount {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 255)
    private String providerAccountId;

    @Column(nullable = false, length = 255)
    private String accountEmail;

    @Column(length = 100)
    private String accountName;

    @Column(nullable = false)
    private boolean isPrimary;

    @Column(nullable = false)
    private boolean syncEnabled;

    @Column(nullable = false, length = 20)
    private String syncStatus;

    @Column(columnDefinition = "CLOB")
    private String accessTokenEncrypted;

    @Column(columnDefinition = "CLOB")
    private String refreshTokenEncrypted;

    private LocalDateTime tokenExpiresAt;

    private LocalDateTime lastSyncedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected MailAccount() {
    }

    public MailAccount(
            String id,
            AppUser user,
            String provider,
            String providerAccountId,
            String accountEmail,
            String accountName,
            boolean isPrimary
    ) {
        this.id = id;
        this.user = user;
        this.provider = provider;
        this.providerAccountId = providerAccountId;
        this.accountEmail = accountEmail;
        this.accountName = accountName;
        this.isPrimary = isPrimary;
        this.syncEnabled = true;
        this.syncStatus = "ACTIVE";
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

    public void updateConnection(
            String accountEmail,
            String accountName,
            String accessTokenEncrypted,
            String refreshTokenEncrypted,
            LocalDateTime tokenExpiresAt
    ) {
        this.accountEmail = accountEmail;
        this.accountName = accountName;
        this.accessTokenEncrypted = accessTokenEncrypted;
        this.refreshTokenEncrypted = refreshTokenEncrypted;
        this.tokenExpiresAt = tokenExpiresAt;
        this.syncEnabled = true;
        this.syncStatus = "ACTIVE";
    }

    public void updateProviderAccountId(String providerAccountId) {
        this.providerAccountId = providerAccountId;
    }

    public void markSynced(LocalDateTime syncedAt) {
        this.lastSyncedAt = syncedAt;
        this.syncStatus = "ACTIVE";
    }

    public String getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderAccountId() {
        return providerAccountId;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public String getAccessTokenEncrypted() {
        return accessTokenEncrypted;
    }

    public String getRefreshTokenEncrypted() {
        return refreshTokenEncrypted;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }
}
