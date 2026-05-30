package com.mailservice.fny.mailbox.dto;

import jakarta.validation.constraints.NotBlank;

public record WeeklyReportWorkspaceStatusRequest(
        @NotBlank String saveStatus
) {
}
