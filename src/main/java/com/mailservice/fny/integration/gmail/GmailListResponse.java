package com.mailservice.fny.integration.gmail;

import java.util.List;

public record GmailListResponse(
        List<GmailMessageRef> messages,
        String nextPageToken,
        Integer resultSizeEstimate
) {
}
