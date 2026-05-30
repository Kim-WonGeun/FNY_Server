package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailboxAttentionService {

    private final EmailMessageRepository emailMessageRepository;
    private final MailboxDetailService mailboxDetailService;

    public MailboxAttentionService(
            EmailMessageRepository emailMessageRepository,
            MailboxDetailService mailboxDetailService
    ) {
        this.emailMessageRepository = emailMessageRepository;
        this.mailboxDetailService = mailboxDetailService;
    }

    @Transactional
    public EmailDetailResponse updateAttentionResolved(String emailId, boolean resolved) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId));
        email.updateAttentionResolved(resolved);
        return mailboxDetailService.getEmailDetail(emailId);
    }

    @Transactional
    public EmailDetailResponse updateAttentionStatus(String emailId, String status) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId));
        email.updateAttentionStatus(status);
        return mailboxDetailService.getEmailDetail(emailId);
    }
}
