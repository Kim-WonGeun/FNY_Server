package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InboxService {

    private final MailboxUserValidator mailboxUserValidator;
    private final EmailMessageRepository emailMessageRepository;
    private final InboxEmailFilter inboxEmailFilter;

    public InboxService(
            MailboxUserValidator mailboxUserValidator,
            EmailMessageRepository emailMessageRepository,
            InboxEmailFilter inboxEmailFilter
    ) {
        this.mailboxUserValidator = mailboxUserValidator;
        this.emailMessageRepository = emailMessageRepository;
        this.inboxEmailFilter = inboxEmailFilter;
    }

    public List<EmailListResponse> getInbox(
            String userId,
            boolean unreadOnly,
            boolean highPriorityOnly,
            boolean needsReplyOnly,
            String query,
            String sender,
            LocalDate startDate,
            LocalDate endDate,
            boolean searchBody
    ) {
        mailboxUserValidator.ensureExists(userId);
        return emailMessageRepository.findMailboxByUserId(userId).stream()
                .filter(email -> inboxEmailFilter.matches(
                        email,
                        unreadOnly,
                        highPriorityOnly,
                        needsReplyOnly,
                        query,
                        sender,
                        startDate,
                        endDate,
                        searchBody
                ))
                .map(EmailListResponse::from)
                .toList();
    }
}
