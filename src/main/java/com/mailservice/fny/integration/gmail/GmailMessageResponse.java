package com.mailservice.fny.integration.gmail;

import java.util.List;

public record GmailMessageResponse(
        String id,
        String threadId,
        List<String> labelIds,
        String snippet,
        String internalDate,
        GmailPayload payload
) {
}
