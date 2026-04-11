package com.mailservice.fny.integration.mail.entity;

public record MailDelivery(
        String to,
        String subject,
        String body,
        boolean html
) {
}
