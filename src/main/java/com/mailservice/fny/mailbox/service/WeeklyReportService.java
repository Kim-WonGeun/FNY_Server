package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
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
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyReportService {

    private static final int MAX_EMAILS = 150;

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final WeeklyMailReportRepository weeklyMailReportRepository;
    private final WeeklyReportWorkspaceService weeklyReportWorkspaceService;
    private final WeeklyReportContentBuilder weeklyReportContentBuilder;
    private final WeeklyReportPayloadMapper weeklyReportPayloadMapper;
    private final WeeklyReportPeriodResolver weeklyReportPeriodResolver;
    private final IdGenerator idGenerator;

    public WeeklyReportService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository,
            EmailMessageRepository emailMessageRepository,
            WeeklyMailReportRepository weeklyMailReportRepository,
            WeeklyReportWorkspaceService weeklyReportWorkspaceService,
            WeeklyReportContentBuilder weeklyReportContentBuilder,
            WeeklyReportPayloadMapper weeklyReportPayloadMapper,
            WeeklyReportPeriodResolver weeklyReportPeriodResolver,
            IdGenerator idGenerator
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.weeklyMailReportRepository = weeklyMailReportRepository;
        this.weeklyReportWorkspaceService = weeklyReportWorkspaceService;
        this.weeklyReportContentBuilder = weeklyReportContentBuilder;
        this.weeklyReportPayloadMapper = weeklyReportPayloadMapper;
        this.weeklyReportPeriodResolver = weeklyReportPeriodResolver;
        this.idGenerator = idGenerator;
    }

    public List<WeeklyReportListItem> listReports(String userId, String mailAccountId) {
        ensureUserExists(userId);
        MailAccount account = mailAccountRepository.findByIdAndUser_Id(mailAccountId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("메일 계정을 찾을 수 없습니다. id=" + mailAccountId));

        List<WeeklyMailReport> reports = weeklyMailReportRepository.findTop20ByMailAccountIdOrderByCreatedAtDesc(account.getId());
        Map<String, String> workspaceStatuses = weeklyReportWorkspaceService.findActiveStatuses(userId, reports);

        return reports.stream()
                .map(report -> WeeklyReportListItem.from(
                        report,
                        workspaceStatuses.getOrDefault(report.getId(), "NONE")
                ))
                .toList();
    }

    public WeeklyReportResponse getReport(String userId, String reportId) {
        ensureUserExists(userId);
        WeeklyMailReport report = weeklyMailReportRepository.findByIdAndMailAccount_User_Id(reportId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("주간 요약을 찾을 수 없습니다. id=" + reportId));

        return weeklyReportPayloadMapper.toResponse(report);
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

        WeeklyReportPeriodRange periodRange = weeklyReportPeriodResolver.resolve(daysParam, startDate, endDate);
        LocalDateTime periodStart = periodRange.start();
        LocalDateTime periodEnd = periodRange.endExclusive();

        List<EmailMessage> emails = emailMessageRepository
                .findByMailAccount_IdAndReceivedAtGreaterThanEqualAndReceivedAtBeforeOrderByReceivedAtDesc(
                        account.getId(),
                        periodStart,
                        periodEnd
                );

        List<EmailMessage> capped = emails.stream().limit(MAX_EMAILS).toList();

        WeeklyReportContent content = weeklyReportContentBuilder.build(account, periodStart, periodEnd, capped, periodRange.label());

        String payloadJson = weeklyReportPayloadMapper.toPayloadJson(content);

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

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }
}
