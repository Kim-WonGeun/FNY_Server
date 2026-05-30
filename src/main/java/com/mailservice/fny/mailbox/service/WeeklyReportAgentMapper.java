package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.infrastructure.AgentWeeklyEmailLineRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportResponse;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyThreadSummaryResponse;
import com.mailservice.fny.analysis.service.PromptTemplateService;
import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportThreadItem;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportAgentMapper {

    private static final int BODY_EXCERPT = 720;

    private final PromptTemplateService promptTemplateService;

    public WeeklyReportAgentMapper(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    AgentWeeklyReportRequest toAgentRequest(
            MailAccount account,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<EmailMessage> emails
    ) {
        List<AgentWeeklyEmailLineRequest> lines = emails.stream()
                .map(email -> new AgentWeeklyEmailLineRequest(
                        email.getId(),
                        Objects.toString(email.getSubject(), ""),
                        excerpt(combinedBody(email), BODY_EXCERPT),
                        email.getFromEmail(),
                        email.getReceivedAt().toString()
                ))
                .toList();

        return new AgentWeeklyReportRequest(
                account.getId(),
                periodStart.toString(),
                periodEnd.toString(),
                "ko",
                promptTemplateService.resolveWeeklyReportPrompt(),
                lines
        );
    }

    WeeklyReportContent toContent(AgentWeeklyReportResponse response) {
        List<WeeklyReportThreadItem> threads = response.threadSummaries() == null
                ? List.of()
                : response.threadSummaries().stream()
                .map(WeeklyReportAgentMapper::mapThread)
                .toList();

        return new WeeklyReportContent(
                "AGENT",
                Optional.ofNullable(response.executiveSummary()).orElse(""),
                nullToList(response.highlights()),
                nullToList(response.risksBlockers()),
                nullToList(response.pendingDecisions()),
                nullToList(response.nextWeekSuggestions()),
                threads,
                Optional.ofNullable(response.modelName()).orElse("rule-based-agent-weekly"),
                Optional.ofNullable(response.promptVersion()).orElse("weekly-rule-v1")
        );
    }

    private static WeeklyReportThreadItem mapThread(AgentWeeklyThreadSummaryResponse row) {
        return new WeeklyReportThreadItem(
                row.emailId(),
                Optional.ofNullable(row.subject()).orElse(""),
                Optional.ofNullable(row.oneLiner()).orElse(""),
                null,
                null,
                "AGENT",
                Optional.ofNullable(row.oneLiner()).orElse("")
        );
    }

    private static String combinedBody(EmailMessage email) {
        String snippet = email.getMessageSnippet();
        String body = email.getBodyText();
        if (body != null && !body.isBlank()) {
            return body;
        }
        return snippet != null ? snippet : "";
    }

    private static String excerpt(String text, int max) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.strip().replaceAll("\\s+", " ");
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max) + "…";
    }

    private static List<String> nullToList(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }
}
