package com.mailservice.fny.integration.mail.dto;

import com.mailservice.fny.integration.mail.entity.MailProviderType;
import com.mailservice.fny.integration.mail.entity.MailSendResult;

public record MailSendResponse(
        String messageId,
        String recipient,
        MailProviderType providerType,
        String status
) {

    public static MailSendResponse from(MailSendResult result) {
        return new MailSendResponse(
                result.messageId(),
                result.recipient(),
                result.providerType(),
                "SENT"
        );
    }
}
