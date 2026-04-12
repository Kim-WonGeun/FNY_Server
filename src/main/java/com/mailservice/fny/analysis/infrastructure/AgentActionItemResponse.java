package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailservice.fny.analysis.dto.AnalysisActionItemRequest;
import java.time.LocalDateTime;

public record AgentActionItemResponse(
        @JsonProperty("action_text") String actionText,
        @JsonProperty("action_type") String actionType,
        @JsonProperty("priority_level") String priorityLevel,
        @JsonProperty("due_at") LocalDateTime dueAt
) {

    public AnalysisActionItemRequest toRequest() {
        return new AnalysisActionItemRequest(actionText, actionType, priorityLevel, dueAt);
    }
}
