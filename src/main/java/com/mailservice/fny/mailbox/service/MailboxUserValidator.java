package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import org.springframework.stereotype.Component;

@Component
public class MailboxUserValidator {

    private final AppUserRepository appUserRepository;

    public MailboxUserValidator(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public void ensureExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw notFound(userId);
        }
    }

    public AppUser getRequiredUser(String userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> notFound(userId));
    }

    private MailboxNotFoundException notFound(String userId) {
        return new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
    }
}
