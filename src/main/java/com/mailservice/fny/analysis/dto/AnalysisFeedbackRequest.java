package com.mailservice.fny.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalysisFeedbackRequest(
        @NotBlank String feedbackType,
        @Size(max = 50) String reasonCode,
        @Size(max = 1000) String memo
) {
}
