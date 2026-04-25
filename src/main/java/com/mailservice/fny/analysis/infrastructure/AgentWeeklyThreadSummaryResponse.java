package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentWeeklyThreadSummaryResponse(
        @JsonProperty("email_id") String emailId,
        String subject,
        @JsonProperty("one_liner") String oneLiner
) {
}
