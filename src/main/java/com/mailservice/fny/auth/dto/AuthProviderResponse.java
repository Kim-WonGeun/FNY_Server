package com.mailservice.fny.auth.dto;

public record AuthProviderResponse(
        String provider,
        String loginUrl,
        String connectUrl
) {
}
