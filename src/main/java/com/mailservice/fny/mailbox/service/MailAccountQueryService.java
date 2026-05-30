package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.MailAccountResponse;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailAccountQueryService {

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;

    public MailAccountQueryService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
    }

    public List<MailAccountResponse> getMailAccounts(String userId) {
        ensureUserExists(userId);
        return mailAccountRepository.findByUserIdOrderByIsPrimaryDescCreatedAtAsc(userId).stream()
                .map(MailAccountResponse::from)
                .toList();
    }

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }
}
