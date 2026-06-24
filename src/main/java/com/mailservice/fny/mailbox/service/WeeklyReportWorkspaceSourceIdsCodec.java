package com.mailservice.fny.mailbox.service;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class WeeklyReportWorkspaceSourceIdsCodec {

    private final ObjectMapper objectMapper;

    public WeeklyReportWorkspaceSourceIdsCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> normalize(List<String> sourceIds) {
        if (sourceIds == null) {
            return List.of();
        }
        return sourceIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
    }

    public String serialize(List<String> sourceIds) {
        try {
            return objectMapper.writeValueAsString(sourceIds);
        } catch (JacksonException exception) {
            throw new IllegalStateException("제외 메일 목록 JSON 직렬화에 실패했습니다.", exception);
        }
    }

    public List<String> deserialize(String value) {
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
