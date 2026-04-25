package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentPromptTemplateRequest(
        @JsonProperty("prompt_code") String promptCode,
        int version,
        @JsonProperty("model_name") String modelName,
        String role,
        String policy,
        String guide,
        String output
) {
}
