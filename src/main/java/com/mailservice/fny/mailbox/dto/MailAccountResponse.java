package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;

public record MailAccountResponse(
        String id,
        String provider,
        String accountEmail,
        String accountName,
        boolean primary,
        boolean syncEnabled,
        String syncStatus,
        LocalDateTime lastSyncedAt
) {

    public static MailAccountResponse from(MailAccount account) {
        return new MailAccountResponse(
                account.getId(),
                account.getProvider(),
                account.getAccountEmail(),
                account.getAccountName(),
                account.isPrimary(),
                account.isSyncEnabled(),
                account.getSyncStatus(),
                account.getLastSyncedAt()
        );
    }
}
