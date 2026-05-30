package com.mailservice.fny.analysis.dto;

import java.time.LocalDateTime;

public record AgentHealthResponse(
        boolean enabled,
        boolean reachable,
        String status,
        String baseUrl,
        String message,
        LocalDateTime checkedAt
) {
}
