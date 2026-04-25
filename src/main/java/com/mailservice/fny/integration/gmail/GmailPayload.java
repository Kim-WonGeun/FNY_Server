package com.mailservice.fny.integration.gmail;

import java.util.List;

public record GmailPayload(
        String mimeType,
        String filename,
        List<GmailHeader> headers,
        GmailBody body,
        List<GmailPayload> parts
) {
}
