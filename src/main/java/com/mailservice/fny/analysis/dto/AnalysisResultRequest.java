package com.mailservice.fny.analysis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AnalysisResultRequest(
        @JsonAlias("email_id") @NotBlank String emailId,
        @JsonAlias("model_name") String modelName,
        @JsonAlias("prompt_version") String promptVersion,
        @JsonAlias("short_summary") @NotBlank String shortSummary,
        @JsonAlias("detailed_summary") String detailedSummary,
        String category,
        @JsonAlias("priority_level") String priorityLevel,
        @JsonAlias("importance_score") @NotNull BigDecimal importanceScore,
        @JsonAlias("urgency_score") @NotNull BigDecimal urgencyScore,
        @JsonAlias("confidence_score") @NotNull BigDecimal confidenceScore,
        @JsonAlias("needs_reply") Boolean needsReply,
        @JsonAlias("has_deadline") Boolean hasDeadline,
        @JsonAlias("deadline_at") LocalDateTime deadlineAt,
        @JsonAlias("deadline_text") String deadlineText,
        @JsonAlias("time_sensitivity") String timeSensitivity,
        @JsonAlias("requires_action") Boolean requiresAction,
        @JsonAlias("user_task_summary") String userTaskSummary,
        @JsonAlias("priority_reason_codes") List<String> priorityReasonCodes,
        @JsonAlias("suggested_action") String suggestedAction,
        String reasoning,
        @JsonAlias("action_items") @Valid List<AnalysisActionItemRequest> actionItems
) {
}
