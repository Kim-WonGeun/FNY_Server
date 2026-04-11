package com.mailservice.fny.integration.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MailSendRequest(
        @NotNull Long integrationId,
        @Email @NotBlank String to,
        @NotBlank String subject,
        @NotBlank String body,
        boolean html
) {
}
