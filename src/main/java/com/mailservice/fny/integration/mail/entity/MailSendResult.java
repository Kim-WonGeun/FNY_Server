package com.mailservice.fny.integration.mail.entity;

public record MailSendResult(
        String messageId,
        String recipient,
        MailProviderType providerType
) {
}
