package com.mailservice.fny.integration.mail.dto;

import com.mailservice.fny.integration.mail.entity.MailIntegration;
import com.mailservice.fny.integration.mail.entity.MailProviderType;
import java.time.LocalDateTime;

public record MailIntegrationResponse(
        Long id,
        MailProviderType providerType,
        String displayName,
        String emailAddress,
        String smtpHost,
        Integer smtpPort,
        String smtpUsername,
        String protocol,
        boolean tlsEnabled,
        LocalDateTime createdAt
) {

    public static MailIntegrationResponse from(MailIntegration integration) {
        return new MailIntegrationResponse(
                integration.getId(),
                integration.getProviderType(),
                integration.getDisplayName(),
                integration.getEmailAddress(),
                integration.getSmtpHost(),
                integration.getSmtpPort(),
                integration.getSmtpUsername(),
                integration.getProtocol(),
                integration.isTlsEnabled(),
                integration.getCreatedAt()
        );
    }
}
