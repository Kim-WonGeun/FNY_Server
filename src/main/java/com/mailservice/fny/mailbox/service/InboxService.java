package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InboxService {

    private final AppUserRepository appUserRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final InboxEmailFilter inboxEmailFilter;

    public InboxService(
            AppUserRepository appUserRepository,
            EmailMessageRepository emailMessageRepository,
            InboxEmailFilter inboxEmailFilter
    ) {
        this.appUserRepository = appUserRepository;
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
        ensureUserExists(userId);
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

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }
}
