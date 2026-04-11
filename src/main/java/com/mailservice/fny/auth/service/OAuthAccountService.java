package com.mailservice.fny.auth.service;

import com.mailservice.fny.auth.dto.AuthSessionResponse;
import com.mailservice.fny.auth.dto.OAuthProvisionRequest;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthAccountService {

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;
    private final IdGenerator idGenerator;

    public OAuthAccountService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository,
            IdGenerator idGenerator
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.idGenerator = idGenerator;
    }

    public AuthSessionResponse provisionAccount(OAuthProvisionRequest request) {
        AppUser user = appUserRepository.findByPrimaryEmail(request.accountEmail())
                .map(existing -> {
                    existing.updateLoginProfile(request.accountName(), request.accountEmail());
                    return existing;
                })
                .orElseGet(() -> appUserRepository.save(new AppUser(
                        idGenerator.generate("USR"),
                        request.accountName(),
                        request.accountEmail(),
                        "ACTIVE",
                        java.time.LocalDateTime.now()
                )));

        MailAccount account = mailAccountRepository
                .findByProviderAndProviderAccountId(normalizeProvider(request.provider()), request.providerAccountId())
                .orElseGet(() -> mailAccountRepository.save(new MailAccount(
                        idGenerator.generate("MAC"),
                        user,
                        normalizeProvider(request.provider()),
                        request.providerAccountId(),
                        request.accountEmail(),
                        request.accountName(),
                        mailAccountRepository.countByUserId(user.getId()) == 0
                )));

        account.updateConnection(
                request.accountEmail(),
                request.accountName(),
                encryptPlaceholder(request.accessToken()),
                encryptPlaceholder(request.refreshToken()),
                request.tokenExpiresAt()
        );

        return new AuthSessionResponse(
                user.getId(),
                user.getDisplayName(),
                user.getPrimaryEmail(),
                account.getId(),
                account.getProvider(),
                account.getAccountEmail()
        );
    }

    private String normalizeProvider(String provider) {
        return provider.trim().toUpperCase();
    }

    private String encryptPlaceholder(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        return "{plain-for-dev}" + token;
    }
}
