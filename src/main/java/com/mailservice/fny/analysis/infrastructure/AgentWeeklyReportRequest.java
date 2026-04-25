package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentWeeklyReportRequest(
        @JsonProperty("mail_account_id") String mailAccountId,
        @JsonProperty("period_start") String periodStart,
        @JsonProperty("period_end") String periodEnd,
        String language,
        AgentPromptTemplateRequest prompt,
        List<AgentWeeklyEmailLineRequest> emails
) {
}
