package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class WeeklyReportPayloadMapper {

    private final ObjectMapper objectMapper;

    public WeeklyReportPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    WeeklyReportResponse toResponse(WeeklyMailReport report) {
        try {
            WeeklyReportContent content = objectMapper.readValue(report.getReportPayload(), WeeklyReportContent.class);
            return WeeklyReportResponse.from(report, content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("저장된 주간 요약을 읽지 못했습니다.", exception);
        }
    }

    String toPayloadJson(WeeklyReportContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("주간 요약 JSON 직렬화에 실패했습니다.", exception);
        }
    }
}
