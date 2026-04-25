package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.infrastructure.AgentAnalysisException;
import com.mailservice.fny.analysis.entity.PromptTemplate;
import com.mailservice.fny.analysis.infrastructure.AgentPromptTemplateRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyEmailLineRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportClient;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportRequest;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyReportResponse;
import com.mailservice.fny.analysis.infrastructure.AgentWeeklyThreadSummaryResponse;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.analysis.repository.PromptTemplateRepository;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
import com.mailservice.fny.mailbox.dto.WeeklyReportThreadItem;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import com.mailservice.fny.mailbox.repository.WeeklyMailReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class WeeklyReportService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportService.class);

    private static final String WEEKLY_REPORT_PROMPT_CODE = "WEEKLY_REPORT";
    private static final String WEEKLY_REPORT_PROMPT_TYPE = "REPORT";
    private static final int MAX_EMAILS = 150;
    private static final int BODY_EXCERPT = 720;
    private static final List<String> WEEKLY_KEYWORDS = List.of(
            "승인", "고객", "미팅", "정산", "세금계산서", "배포", "QA", "계약", "장애", "보안",
            "예산", "릴리즈", "파트너", "마케팅", "데이터", "인프라", "결제", "운영", "정책", "성과",
            "회의록", "일정", "검토", "확인", "요청", "보고", "공유", "권한", "알림"
    );
    private static final Set<String> WEEKLY_STOPWORDS = Set.of(
            "안녕하세요", "부탁드립니다", "공유드립니다", "확인해", "주세요", "이번", "다음", "오늘",
            "관련", "필요합니다", "있습니다", "합니다"
    );

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailAnalysisRepository emailAnalysisRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final WeeklyMailReportRepository weeklyMailReportRepository;
    private final AgentWeeklyReportClient agentWeeklyReportClient;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final boolean agentEnabled;

    public WeeklyReportService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository,
            EmailMessageRepository emailMessageRepository,
            EmailAnalysisRepository emailAnalysisRepository,
            PromptTemplateRepository promptTemplateRepository,
            WeeklyMailReportRepository weeklyMailReportRepository,
            AgentWeeklyReportClient agentWeeklyReportClient,
            ObjectMapper objectMapper,
            IdGenerator idGenerator,
            @Value("${fny.agent.enabled:false}") boolean agentEnabled
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.promptTemplateRepository = promptTemplateRepository;
        this.weeklyMailReportRepository = weeklyMailReportRepository;
        this.agentWeeklyReportClient = agentWeeklyReportClient;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.agentEnabled = agentEnabled;
    }

    public List<WeeklyReportListItem> listReports(String userId, String mailAccountId) {
        ensureUserExists(userId);
        MailAccount account = mailAccountRepository.findByIdAndUser_Id(mailAccountId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("메일 계정을 찾을 수 없습니다. id=" + mailAccountId));

        return weeklyMailReportRepository.findTop20ByMailAccountIdOrderByCreatedAtDesc(account.getId()).stream()
                .map(WeeklyReportListItem::from)
                .toList();
    }

    public WeeklyReportResponse getReport(String userId, String reportId) {
        ensureUserExists(userId);
        WeeklyMailReport report = weeklyMailReportRepository.findByIdAndMailAccount_User_Id(reportId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("주간 요약을 찾을 수 없습니다. id=" + reportId));

        try {
            WeeklyReportContent content = objectMapper.readValue(report.getReportPayload(), WeeklyReportContent.class);
            return WeeklyReportResponse.from(report, content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("저장된 주간 요약을 읽지 못했습니다.", exception);
        }
    }

    @Transactional
    public WeeklyReportResponse generate(String userId, String mailAccountId, int daysParam) {
        return generate(userId, mailAccountId, daysParam, "WEEKLY", null, null);
    }

    @Transactional
    public WeeklyReportResponse generate(
            String userId,
            String mailAccountId,
            int daysParam,
            String reportType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ensureUserExists(userId);
        MailAccount account = mailAccountRepository.findByIdAndUser_Id(mailAccountId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("메일 계정을 찾을 수 없습니다. id=" + mailAccountId));

        PeriodRange periodRange = resolvePeriodRange(daysParam, startDate, endDate);
        LocalDateTime periodStart = periodRange.start();
        LocalDateTime periodEnd = periodRange.endExclusive();

        List<EmailMessage> emails = emailMessageRepository
                .findByMailAccount_IdAndReceivedAtGreaterThanEqualAndReceivedAtBeforeOrderByReceivedAtDesc(
                        account.getId(),
                        periodStart,
                        periodEnd
                );

        List<EmailMessage> capped = emails.stream().limit(MAX_EMAILS).toList();

        WeeklyReportContent content = buildContent(account, periodStart, periodEnd, capped, periodRange.label());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(content);
        } catch (JacksonException exception) {
            throw new IllegalStateException("주간 요약 JSON 직렬화에 실패했습니다.", exception);
        }

        String reportId = idGenerator.generate("WKB");
        WeeklyMailReport entity = new WeeklyMailReport(
                reportId,
                account,
                periodStart,
                periodEnd,
                capped.size(),
                "COMPLETED",
                normalizeReportType(reportType),
                content.executiveSummary(),
                payloadJson
        );
        weeklyMailReportRepository.save(entity);

        return WeeklyReportResponse.from(entity, content);
    }

    private String normalizeReportType(String reportType) {
        if (reportType == null || reportType.isBlank()) {
            return "WEEKLY";
        }
        return switch (reportType.trim().toUpperCase()) {
            case "PROGRESS" -> "PROGRESS";
            case "ISSUE" -> "ISSUE";
            default -> "WEEKLY";
        };
    }

    private WeeklyReportContent buildContent(
            MailAccount account,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<EmailMessage> capped,
            String periodLabel
    ) {
        if (capped.isEmpty()) {
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
                AgentWeeklyReportRequest request = buildAgentRequest(account, periodStart, periodEnd, capped);
                AgentWeeklyReportResponse response = agentWeeklyReportClient.analyzeWeekly(request);
                return mapFromAgent(response);
            } catch (AgentAnalysisException exception) {
                log.warn("Agent 주간 요약 요청 실패. status={}, body={}", exception.getStatusCode(), exception.getResponseBody());
            } catch (RuntimeException exception) {
                log.warn("Agent 주간 요약 요청 실패. message={}", exception.getMessage());
            }
        }

        return buildFallback(capped, periodLabel);
    }

    private AgentWeeklyReportRequest buildAgentRequest(
            MailAccount account,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<EmailMessage> capped
    ) {
        List<AgentWeeklyEmailLineRequest> lines = capped.stream()
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
                resolvePromptTemplate(),
                lines
        );
    }

    private AgentPromptTemplateRequest resolvePromptTemplate() {
        return promptTemplateRepository
                .findFirstByPromptCodeAndPromptTypeAndActiveTrueOrderByVersionDesc(
                        WEEKLY_REPORT_PROMPT_CODE,
                        WEEKLY_REPORT_PROMPT_TYPE
                )
                .map(WeeklyReportService::toAgentPrompt)
                .orElseGet(WeeklyReportService::fallbackPrompt);
    }

    private static AgentPromptTemplateRequest toAgentPrompt(PromptTemplate prompt) {
        return new AgentPromptTemplateRequest(
                prompt.getPromptCode(),
                prompt.getVersion(),
                prompt.getModelName(),
                prompt.getRoleContent(),
                prompt.getPolicyContent(),
                prompt.getGuideContent(),
                prompt.getOutputContent()
        );
    }

    private static AgentPromptTemplateRequest fallbackPrompt() {
        return new AgentPromptTemplateRequest(
                WEEKLY_REPORT_PROMPT_CODE,
                1,
                "gpt-5.4-mini",
                "너는 업무 메일을 근거로 주간보고 초안을 작성하는 비서다.",
                "메일 원문에 없는 사실을 만들지 말고, 불확실한 내용은 확인필요로 분류한다.",
                "금주실적, 진행사항, 특이사항, 확인필요, 차주계획을 업무 보고 문체로 작성한다.",
                "executive_summary, highlights, risks_blockers, pending_decisions, next_week_suggestions, thread_summaries JSON만 반환한다."
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

    private WeeklyReportContent mapFromAgent(AgentWeeklyReportResponse response) {
        List<WeeklyReportThreadItem> threads = response.threadSummaries() == null
                ? List.of()
                : response.threadSummaries().stream()
                .map(WeeklyReportService::mapThread)
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
                Optional.ofNullable(row.oneLiner()).orElse("")
        );
    }

    private static List<String> nullToList(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    private WeeklyReportContent buildFallback(List<EmailMessage> capped, String periodLabel) {
        StringBuilder corpus = new StringBuilder();
        List<WeeklyReportThreadItem> threads = new ArrayList<>();

        for (EmailMessage email : capped) {
            String subject = email.getSubject() != null ? email.getSubject() : "(제목 없음)";
            String text = subject + "\n" + combinedBody(email);
            corpus.append(text).append('\n');
            String oneLiner = String.join(", ", extractKeywords(text, 6));
            threads.add(new WeeklyReportThreadItem(email.getId(), subject, oneLiner));
        }

        List<String> keywords = extractKeywords(corpus.toString(), 24);
        List<String> issueKeywords = keywordGroup(keywords, Set.of("장애", "보안", "계약", "정산", "세금계산서", "예산", "결제", "인프라"));
        List<String> workKeywords = keywordGroup(keywords, Set.of("승인", "검토", "확인", "요청", "일정", "회의록", "권한", "정책"));
        List<String> topicKeywords = keywordGroup(keywords, Set.of("고객", "미팅", "배포", "QA", "릴리즈", "파트너", "마케팅", "데이터", "운영", "성과", "보고", "공유"));
        String executive = periodLabel + " 수신 메일 " + capped.size() + "건을 바탕으로 주간보고 초안을 구성했습니다.";

        return new WeeklyReportContent(
                "FALLBACK",
                executive,
                List.of(
                        reportLine("금주실적", !topicKeywords.isEmpty() ? topicKeywords : keywords, "금주실적: 선택 기간 메일에서 주요 완료 내용을 추가 확인해 주세요."),
                        reportLine("진행사항", !workKeywords.isEmpty() ? workKeywords : keywords, "진행사항: 검토 중인 업무를 메일 원문 기준으로 보강해 주세요.")
                ),
                List.of(reportLine("특이사항", issueKeywords, "특이사항: 별도 이슈로 분류할 키워드가 뚜렷하지 않습니다.")),
                List.of(reportLine("확인필요", workKeywords, "확인필요: 추가 확인이 필요한 항목이 뚜렷하지 않습니다.")),
                List.of(reportLine("차주계획", mergeLimited(workKeywords, topicKeywords, 8), "차주계획: 금주 메일 흐름을 바탕으로 다음 주 계획을 보강해 주세요.")),
                threads.stream().limit(40).toList(),
                "server-fallback",
                "weekly-report-v1"
        );
    }

    private static List<String> extractKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        for (String keyword : WEEKLY_KEYWORDS) {
            int count = countOccurrences(normalized, keyword.toLowerCase());
            if (count > 0) {
                scores.merge(keyword, count * 3, Integer::sum);
            }
        }

        for (String token : text.split("[^가-힣A-Za-z0-9]+")) {
            if (token.length() < 2 || WEEKLY_STOPWORDS.contains(token) || WEEKLY_STOPWORDS.contains(token.toLowerCase())) {
                continue;
            }
            scores.merge(token, 1, Integer::sum);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = text.indexOf(keyword);
        while (index >= 0) {
            count++;
            index = text.indexOf(keyword, index + keyword.length());
        }
        return count;
    }

    private static List<String> keywordGroup(List<String> keywords, Set<String> candidates) {
        return keywords.stream()
                .filter(candidates::contains)
                .limit(10)
                .toList();
    }

    private static String reportLine(String prefix, List<String> keywords, String fallback) {
        List<String> selected = keywords.stream().limit(5).toList();
        if (selected.isEmpty()) {
            return fallback;
        }
        return prefix + ": " + String.join(", ", selected) + " 관련 내용을 정리했습니다.";
    }

    private static List<String> mergeLimited(List<String> first, List<String> second, int limit) {
        List<String> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged.stream().distinct().limit(limit).toList();
    }

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }

    private static PeriodRange resolvePeriodRange(int daysParam, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            int days = Math.max(1, Math.min(daysParam, 30));
            LocalDateTime periodEnd = LocalDateTime.now();
            return new PeriodRange(periodEnd.minusDays(days), periodEnd, "최근 " + days + "일간");
        }

        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일과 종료일을 모두 입력해야 합니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        if (startDate.plusDays(30).isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간은 최대 31일까지 가능합니다.");
        }

        return new PeriodRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                startDate + " ~ " + endDate
        );
    }

    private record PeriodRange(
            LocalDateTime start,
            LocalDateTime endExclusive,
            String label
    ) {
    }
}
