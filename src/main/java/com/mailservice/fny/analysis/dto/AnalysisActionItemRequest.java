package com.mailservice.fny.analysis.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record AnalysisActionItemRequest(
        @JsonAlias("action_text") @NotBlank String actionText,
        @JsonAlias("action_type") String actionType,
        @JsonAlias("priority_level") String priorityLevel,
        @JsonAlias("due_at") LocalDateTime dueAt
) {
}
