package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AgentWeeklyReportResponse(
        @JsonProperty("executive_summary") String executiveSummary,
        List<String> highlights,
        @JsonProperty("risks_blockers") List<String> risksBlockers,
        @JsonProperty("pending_decisions") List<String> pendingDecisions,
        @JsonProperty("next_week_suggestions") List<String> nextWeekSuggestions,
        @JsonProperty("thread_summaries") List<AgentWeeklyThreadSummaryResponse> threadSummaries,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("prompt_version") String promptVersion
) {
}
