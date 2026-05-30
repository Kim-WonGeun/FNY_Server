package com.mailservice.fny.mailbox.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record WeeklyReportWorkspaceRequest(
        @NotBlank String draftText,
        String saveStatus,
        List<String> excludedSourceIds
) {
}
