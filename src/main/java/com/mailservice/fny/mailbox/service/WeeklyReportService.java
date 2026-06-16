package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyMailReportType;
import com.mailservice.fny.mailbox.repository.WeeklyMailReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyReportService {

    private final MailboxResourceResolver mailboxResourceResolver;
    private final WeeklyMailReportRepository weeklyMailReportRepository;
    private final WeeklyReportListQueryService weeklyReportListQueryService;
    private final WeeklyReportEmailQueryService weeklyReportEmailQueryService;
    private final WeeklyReportContentBuilder weeklyReportContentBuilder;
    private final WeeklyReportPayloadMapper weeklyReportPayloadMapper;
    private final WeeklyReportPeriodResolver weeklyReportPeriodResolver;
    private final WeeklyMailReportFactory weeklyMailReportFactory;

    public WeeklyReportService(
            MailboxResourceResolver mailboxResourceResolver,
            WeeklyMailReportRepository weeklyMailReportRepository,
            WeeklyReportListQueryService weeklyReportListQueryService,
            WeeklyReportEmailQueryService weeklyReportEmailQueryService,
            WeeklyReportContentBuilder weeklyReportContentBuilder,
            WeeklyReportPayloadMapper weeklyReportPayloadMapper,
            WeeklyReportPeriodResolver weeklyReportPeriodResolver,
            WeeklyMailReportFactory weeklyMailReportFactory
    ) {
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.weeklyMailReportRepository = weeklyMailReportRepository;
        this.weeklyReportListQueryService = weeklyReportListQueryService;
        this.weeklyReportEmailQueryService = weeklyReportEmailQueryService;
        this.weeklyReportContentBuilder = weeklyReportContentBuilder;
        this.weeklyReportPayloadMapper = weeklyReportPayloadMapper;
        this.weeklyReportPeriodResolver = weeklyReportPeriodResolver;
        this.weeklyMailReportFactory = weeklyMailReportFactory;
    }

    public List<WeeklyReportListItem> listReports(String userId, String mailAccountId) {
        MailAccount account = mailboxResourceResolver.getRequiredMailAccount(userId, mailAccountId);

        return weeklyReportListQueryService.findListItems(userId, account.getId());
    }

    public WeeklyReportResponse getReport(String userId, String reportId) {
        WeeklyMailReport report = mailboxResourceResolver.getRequiredWeeklyReport(userId, reportId);

        return weeklyReportPayloadMapper.toResponse(report);
    }

    @Transactional
    public WeeklyReportResponse generate(String userId, String mailAccountId, int daysParam) {
        return generate(userId, mailAccountId, daysParam, WeeklyMailReportType.WEEKLY, null, null);
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
        MailAccount account = mailboxResourceResolver.getRequiredMailAccount(userId, mailAccountId);

        WeeklyReportPeriodRange periodRange = weeklyReportPeriodResolver.resolve(daysParam, startDate, endDate);
        LocalDateTime periodStart = periodRange.start();
        LocalDateTime periodEnd = periodRange.endExclusive();

        List<EmailMessage> emails = weeklyReportEmailQueryService.findReportEmails(account.getId(), periodStart, periodEnd);

        WeeklyReportContent content = weeklyReportContentBuilder.build(account, periodStart, periodEnd, emails, periodRange.label());

        String payloadJson = weeklyReportPayloadMapper.toPayloadJson(content);

        WeeklyMailReport entity = weeklyMailReportFactory.completedReport(
                account,
                periodStart,
                periodEnd,
                emails.size(),
                reportType,
                content,
                payloadJson
        );
        weeklyMailReportRepository.save(entity);

        return WeeklyReportResponse.from(entity, content);
    }
}
