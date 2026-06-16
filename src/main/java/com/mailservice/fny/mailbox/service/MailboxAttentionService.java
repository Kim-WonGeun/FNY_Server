package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailboxAttentionService {

    private final MailboxResourceResolver mailboxResourceResolver;
    private final MailboxDetailService mailboxDetailService;

    public MailboxAttentionService(
            MailboxResourceResolver mailboxResourceResolver,
            MailboxDetailService mailboxDetailService
    ) {
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.mailboxDetailService = mailboxDetailService;
    }

    @Transactional
    public EmailDetailResponse updateAttentionResolved(String emailId, boolean resolved) {
        EmailMessage email = mailboxResourceResolver.getRequiredEmail(emailId);
        email.updateAttentionResolved(resolved);
        return mailboxDetailService.getEmailDetail(emailId);
    }

    @Transactional
    public EmailDetailResponse updateAttentionStatus(String emailId, String status) {
        EmailMessage email = mailboxResourceResolver.getRequiredEmail(emailId);
        email.updateAttentionStatus(status);
        return mailboxDetailService.getEmailDetail(emailId);
    }
}
