package com.mailservice.fny.auth.service;

import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public static final String USER_ID_HEADER = "X-FNY-USER-ID";

    private final AppUserRepository appUserRepository;
    private final boolean devUserHeaderEnabled;

    public CurrentUserService(
            AppUserRepository appUserRepository,
            @Value("${fny.security.dev-user-header-enabled:false}") boolean devUserHeaderEnabled
    ) {
        this.appUserRepository = appUserRepository;
        this.devUserHeaderEnabled = devUserHeaderEnabled;
    }

    public String resolveUserId(Authentication authentication, HttpServletRequest request) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User principal) {
            Object email = principal.getAttribute("email");
            if (email instanceof String value && !value.isBlank()) {
                return appUserRepository.findByPrimaryEmail(value)
                        .map(AppUser::getId)
                        .orElseThrow(() -> new MailboxNotFoundException("로그인 사용자를 찾을 수 없습니다."));
            }
        }

        String fallbackUserId = request.getHeader(USER_ID_HEADER);
        if (devUserHeaderEnabled && fallbackUserId != null && !fallbackUserId.isBlank()) {
            return fallbackUserId.trim();
        }

        throw new AuthenticationCredentialsNotFoundException("현재 사용자 정보를 확인할 수 없습니다.");
    }
}
