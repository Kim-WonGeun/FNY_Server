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
        String provider = normalizeProvider(request.provider());
        String providerAccountId = request.providerAccountId().trim();
        String accountEmail = request.accountEmail().trim();
        String accountName = request.accountName().trim();

        AppUser user = appUserRepository.findByPrimaryEmail(accountEmail)
                .map(existing -> {
                    existing.updateLoginProfile(accountName, accountEmail);
                    return existing;
                })
                .orElseGet(() -> appUserRepository.save(new AppUser(
                        idGenerator.generate("USR"),
                        accountName,
                        accountEmail,
                        "ACTIVE",
                        java.time.LocalDateTime.now()
                )));

        MailAccount account = mailAccountRepository
                .findByProviderAndProviderAccountId(provider, providerAccountId)
                .or(() -> mailAccountRepository.findByProviderAndAccountEmailIgnoreCase(provider, accountEmail))
                .orElseGet(() -> mailAccountRepository.save(new MailAccount(
                        idGenerator.generate("MAC"),
                        user,
                        provider,
                        providerAccountId,
                        accountEmail,
                        accountName,
                        mailAccountRepository.countByUserId(user.getId()) == 0
                )));

        if (!account.getProviderAccountId().equals(providerAccountId)) {
            account.updateProviderAccountId(providerAccountId);
        }
        account.updateConnection(
                accountEmail,
                accountName,
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
