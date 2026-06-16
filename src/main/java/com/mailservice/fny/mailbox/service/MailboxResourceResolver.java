package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import com.mailservice.fny.mailbox.repository.WeeklyMailReportRepository;
import org.springframework.stereotype.Component;

@Component
public class MailboxResourceResolver {

    private final MailboxUserValidator mailboxUserValidator;
    private final EmailMessageRepository emailMessageRepository;
    private final MailAccountRepository mailAccountRepository;
    private final WeeklyMailReportRepository weeklyMailReportRepository;

    public MailboxResourceResolver(
            MailboxUserValidator mailboxUserValidator,
            EmailMessageRepository emailMessageRepository,
            MailAccountRepository mailAccountRepository,
            WeeklyMailReportRepository weeklyMailReportRepository
    ) {
        this.mailboxUserValidator = mailboxUserValidator;
        this.emailMessageRepository = emailMessageRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.weeklyMailReportRepository = weeklyMailReportRepository;
    }

    public EmailMessage getRequiredEmail(String emailId) {
        return emailMessageRepository.findById(emailId)
                .orElseThrow(() -> emailNotFound(emailId));
    }

    public EmailMessage getRequiredEmailWithMailAccount(String emailId) {
        return emailMessageRepository.findWithMailAccountById(emailId)
                .orElseThrow(() -> emailNotFound(emailId));
    }

    public void ensureEmailExists(String emailId) {
        if (!emailMessageRepository.existsById(emailId)) {
            throw emailNotFound(emailId);
        }
    }

    public MailAccount getRequiredMailAccount(String userId, String mailAccountId) {
        mailboxUserValidator.ensureExists(userId);
        return mailAccountRepository.findByIdAndUser_Id(mailAccountId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("메일 계정을 찾을 수 없습니다. id=" + mailAccountId));
    }

    public WeeklyMailReport getRequiredWeeklyReport(String userId, String reportId) {
        mailboxUserValidator.ensureExists(userId);
        return weeklyMailReportRepository.findByIdAndMailAccount_User_Id(reportId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("주간 요약을 찾을 수 없습니다. id=" + reportId));
    }

    private MailboxNotFoundException emailNotFound(String emailId) {
        return new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId);
    }
}
