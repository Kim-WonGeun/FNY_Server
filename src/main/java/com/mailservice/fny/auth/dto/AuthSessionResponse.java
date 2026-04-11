package com.mailservice.fny.auth.dto;

public record AuthSessionResponse(
        String userId,
        String displayName,
        String primaryEmail,
        String mailAccountId,
        String provider,
        String accountEmail
) {
}
