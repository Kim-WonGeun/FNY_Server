package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailservice.fny.analysis.dto.AnalysisResultRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AgentAnalysisResponse(
        @JsonProperty("email_id") String emailId,
        String urgency,
        @JsonProperty("short_summary") String shortSummary,
        @JsonProperty("detailed_summary") String detailedSummary,
        String category,
        @JsonProperty("priority_level") String priorityLevel,
        @JsonProperty("importance_score") BigDecimal importanceScore,
        @JsonProperty("urgency_score") BigDecimal urgencyScore,
        @JsonProperty("confidence_score") BigDecimal confidenceScore,
        @JsonProperty("needs_reply") Boolean needsReply,
        @JsonProperty("has_deadline") Boolean hasDeadline,
        @JsonProperty("deadline_at") LocalDateTime deadlineAt,
        @JsonProperty("deadline_text") String deadlineText,
        @JsonProperty("time_sensitivity") String timeSensitivity,
        @JsonProperty("requires_action") Boolean requiresAction,
        @JsonProperty("user_task_summary") String userTaskSummary,
        @JsonProperty("priority_reason_codes") List<String> priorityReasonCodes,
        @JsonProperty("suggested_action") String suggestedAction,
        String reasoning,
        @JsonProperty("action_items") List<AgentActionItemResponse> actionItems,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("prompt_version") String promptVersion
) {

    public AnalysisResultRequest toRequest() {
        return new AnalysisResultRequest(
                emailId,
                modelName,
                promptVersion,
                shortSummary,
                detailedSummary,
                category,
                priorityLevel,
                importanceScore,
                urgencyScore,
                confidenceScore,
                needsReply,
                hasDeadline,
                deadlineAt,
                deadlineText,
                timeSensitivity,
                requiresAction,
                userTaskSummary,
                priorityReasonCodes,
                suggestedAction,
                reasoning,
                actionItems == null ? List.of() : actionItems.stream()
                        .map(AgentActionItemResponse::toRequest)
                        .toList()
        );
    }
}
