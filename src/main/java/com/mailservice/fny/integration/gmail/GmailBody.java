package com.mailservice.fny.integration.gmail;

public record GmailBody(
        String attachmentId,
        String data,
        Integer size
) {
}
