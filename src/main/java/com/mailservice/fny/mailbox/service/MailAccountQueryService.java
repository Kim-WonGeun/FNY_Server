package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.MailAccountResponse;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailAccountQueryService {

    private final MailboxUserValidator mailboxUserValidator;
    private final MailAccountRepository mailAccountRepository;

    public MailAccountQueryService(
            MailboxUserValidator mailboxUserValidator,
            MailAccountRepository mailAccountRepository
    ) {
        this.mailboxUserValidator = mailboxUserValidator;
        this.mailAccountRepository = mailAccountRepository;
    }

    public List<MailAccountResponse> getMailAccounts(String userId) {
        mailboxUserValidator.ensureExists(userId);
        return mailAccountRepository.findByUserIdOrderByIsPrimaryDescCreatedAtAsc(userId).stream()
                .map(MailAccountResponse::from)
                .toList();
    }
}
