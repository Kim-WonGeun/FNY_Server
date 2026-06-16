package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyMailReportStatus;
import com.mailservice.fny.mailbox.entity.WeeklyMailReportType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMailReportFactory {

    private final IdGenerator idGenerator;

    public WeeklyMailReportFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public WeeklyMailReport completedReport(
            MailAccount account,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            int emailCount,
            String reportType,
            WeeklyReportContent content,
            String payloadJson
    ) {
        return new WeeklyMailReport(
                idGenerator.generate("WKB"),
                account,
                periodStart,
                periodEnd,
                emailCount,
                WeeklyMailReportStatus.COMPLETED,
                WeeklyMailReportType.normalize(reportType),
                content.executiveSummary(),
                payloadJson
        );
    }
}
