package com.mailservice.fny.integration.mail.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "mail_integrations")
public class MailIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MailProviderType providerType;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 150)
    private String emailAddress;

    @Column(nullable = false, length = 150)
    private String smtpHost;

    @Column(nullable = false)
    private Integer smtpPort;

    @Column(nullable = false, length = 150)
    private String smtpUsername;

    @Column(nullable = false, length = 255)
    private String smtpPassword;

    @Column(nullable = false, length = 20)
    private String protocol;

    @Column(nullable = false)
    private boolean tlsEnabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected MailIntegration() {
    }

    public MailIntegration(
            MailProviderType providerType,
            String displayName,
            String emailAddress,
            String smtpHost,
            Integer smtpPort,
            String smtpUsername,
            String smtpPassword,
            String protocol,
            boolean tlsEnabled
    ) {
        this.providerType = providerType;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.protocol = protocol;
        this.tlsEnabled = tlsEnabled;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public MailProviderType getProviderType() {
        return providerType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
