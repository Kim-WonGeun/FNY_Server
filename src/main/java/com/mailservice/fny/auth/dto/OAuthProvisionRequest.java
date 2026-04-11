package com.mailservice.fny.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record OAuthProvisionRequest(
        @NotBlank String provider,
        @NotBlank String providerAccountId,
        @Email @NotBlank String accountEmail,
        @NotBlank String accountName,
        String accessToken,
        String refreshToken,
        LocalDateTime tokenExpiresAt
) {
}
