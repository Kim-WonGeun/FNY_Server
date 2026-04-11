package com.mailservice.fny.integration.mail.dto;

import com.mailservice.fny.integration.mail.entity.MailProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MailIntegrationCreateRequest(
        @NotNull MailProviderType providerType,
        @NotBlank String displayName,
        @Email @NotBlank String emailAddress,
        @NotBlank String smtpHost,
        @NotNull @Min(1) @Max(65535) Integer smtpPort,
        @NotBlank String smtpUsername,
        @NotBlank String smtpPassword,
        @NotBlank String protocol,
        boolean tlsEnabled
) {
}
