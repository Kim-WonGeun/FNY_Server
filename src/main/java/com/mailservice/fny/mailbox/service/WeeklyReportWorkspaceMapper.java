package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class WeeklyReportWorkspaceMapper {

    private final ObjectMapper objectMapper;

    public WeeklyReportWorkspaceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    WeeklyReportWorkspaceResponse toResponse(WeeklyReportWorkspace workspace) {
        return WeeklyReportWorkspaceResponse.from(
                workspace,
                parseExcludedSourceIds(workspace.getExcludedSourceIds())
        );
    }

    List<String> normalizeExcludedSourceIds(List<String> excludedSourceIds) {
        if (excludedSourceIds == null) {
            return List.of();
        }
        return excludedSourceIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
    }

    String serializeExcludedSourceIds(List<String> excludedSourceIds) {
        try {
            return objectMapper.writeValueAsString(excludedSourceIds);
        } catch (JacksonException exception) {
            throw new IllegalStateException("제외 메일 목록 JSON 직렬화에 실패했습니다.", exception);
        }
    }

    private List<String> parseExcludedSourceIds(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JacksonException exception) {
            return List.of();
        }
    }
}
