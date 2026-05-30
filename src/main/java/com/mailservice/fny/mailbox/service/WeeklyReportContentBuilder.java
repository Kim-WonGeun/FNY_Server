package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.infrastructure.AgentAnalysisException;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportClient;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportResponse;
import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeeklyReportContentBuilder {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportContentBuilder.class);

    private final WeeklyReportFallbackBuilder weeklyReportFallbackBuilder;
    private final AgentWeeklyReportClient agentWeeklyReportClient;
    private final WeeklyReportAgentMapper weeklyReportAgentMapper;
    private final boolean agentEnabled;

    public WeeklyReportContentBuilder(
            WeeklyReportFallbackBuilder weeklyReportFallbackBuilder,
            AgentWeeklyReportClient agentWeeklyReportClient,
            WeeklyReportAgentMapper weeklyReportAgentMapper,
            @Value("${fny.agent.enabled:false}") boolean agentEnabled
    ) {
        this.weeklyReportFallbackBuilder = weeklyReportFallbackBuilder;
        this.agentWeeklyReportClient = agentWeeklyReportClient;
        this.weeklyReportAgentMapper = weeklyReportAgentMapper;
        this.agentEnabled = agentEnabled;
    }

    public WeeklyReportContent build(
            MailAccount account,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<EmailMessage> emails,
            String periodLabel
    ) {
        if (emails.isEmpty()) {
            return new WeeklyReportContent(
                    "FALLBACK",
                    "선택한 기간(" + periodLabel + ")에 수신된 메일이 없습니다.",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    "server-fallback",
                    "weekly-fallback-v1"
            );
        }

        if (agentEnabled) {
            try {
                AgentWeeklyReportRequest request = weeklyReportAgentMapper.toAgentRequest(
                        account,
                        periodStart,
                        periodEnd,
                        emails
                );
                AgentWeeklyReportResponse response = agentWeeklyReportClient.analyzeWeekly(request);
                return weeklyReportAgentMapper.toContent(response);
            } catch (AgentAnalysisException exception) {
                log.warn("Agent 주간 요약 요청 실패. status={}, body={}", exception.getStatusCode(), exception.getResponseBody());
            } catch (RuntimeException exception) {
                log.warn("Agent 주간 요약 요청 실패. message={}", exception.getMessage());
            }
        }

        return weeklyReportFallbackBuilder.build(emails, periodLabel);
    }
}
