package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentWeeklyEmailLineRequest(
        @JsonProperty("email_id") String emailId,
        String subject,
        @JsonProperty("body_excerpt") String bodyExcerpt,
        @JsonProperty("from_email") String fromEmail,
        @JsonProperty("received_at") String receivedAt
) {
}
