package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WeeklyReportEmailQueryService {

    private static final int MAX_EMAILS = 150;

    private final EmailMessageRepository emailMessageRepository;

    public WeeklyReportEmailQueryService(EmailMessageRepository emailMessageRepository) {
        this.emailMessageRepository = emailMessageRepository;
    }

    public List<EmailMessage> findReportEmails(
            String mailAccountId,
            LocalDateTime periodStartInclusive,
            LocalDateTime periodEndExclusive
    ) {
        return emailMessageRepository
                .findByMailAccount_IdAndReceivedAtGreaterThanEqualAndReceivedAtBeforeOrderByReceivedAtDesc(
                        mailAccountId,
                        periodStartInclusive,
                        periodEndExclusive
                )
                .stream()
                .limit(MAX_EMAILS)
                .toList();
    }
}
