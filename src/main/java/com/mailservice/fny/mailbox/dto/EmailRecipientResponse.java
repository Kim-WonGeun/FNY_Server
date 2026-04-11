package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.EmailRecipient;

public record EmailRecipientResponse(
        String type,
        String name,
        String email
) {

    public static EmailRecipientResponse from(EmailRecipient recipient) {
        return new EmailRecipientResponse(
                recipient.getRecipientType(),
                recipient.getRecipientName(),
                recipient.getRecipientEmail()
        );
    }
}
