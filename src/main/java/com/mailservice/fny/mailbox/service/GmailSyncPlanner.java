package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GmailSyncPlanner {

    private static final int MAX_LIMIT = 1000;
    private static final int INCREMENTAL_LOOKBACK_DAYS = 2;
    private static final String DEV_TOKEN_PREFIX = "{plain-for-dev}";

    GmailSyncPlan plan(MailAccount account, int limitParam) {
        if (!"GOOGLE".equalsIgnoreCase(account.getProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gmail 계정만 동기화할 수 있습니다.");
        }

        String accessToken = decryptPlaceholder(account.getAccessTokenEncrypted());
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google access token이 없습니다. 다시 로그인해 주세요.");
        }

        boolean syncAll = limitParam <= 0;
        int requestedLimit = syncAll ? Integer.MAX_VALUE : Math.max(1, Math.min(limitParam, MAX_LIMIT));
        return new GmailSyncPlan(accessToken, syncAll, requestedLimit, resolveGmailQuery(account));
    }

    private String resolveGmailQuery(MailAccount account) {
        if (account.getLastSyncedAt() == null) {
            return null;
        }
        LocalDate afterDate = account.getLastSyncedAt()
                .minusDays(INCREMENTAL_LOOKBACK_DAYS)
                .toLocalDate();
        return "after:" + afterDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    private static String decryptPlaceholder(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith(DEV_TOKEN_PREFIX)) {
            return token.substring(DEV_TOKEN_PREFIX.length());
        }
        return token;
    }
}

record GmailSyncPlan(
        String accessToken,
        boolean syncAll,
        int requestedLimit,
        String gmailQuery
) {
}
