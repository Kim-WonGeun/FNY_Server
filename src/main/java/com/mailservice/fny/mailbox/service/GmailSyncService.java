package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.integration.gmail.GmailClient;
import com.mailservice.fny.integration.gmail.GmailListResponse;
import com.mailservice.fny.integration.gmail.GmailMessageRef;
import com.mailservice.fny.integration.gmail.GmailMessageResponse;
import com.mailservice.fny.mailbox.dto.MailSyncResponse;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GmailSyncService {

    private static final int GMAIL_PAGE_SIZE = 100;

    private final MailboxResourceResolver mailboxResourceResolver;
    private final MailAccountRepository mailAccountRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final GmailClient gmailClient;
    private final TransactionTemplate transactionTemplate;
    private final MailboxDeduplicationService mailboxDeduplicationService;
    private final GmailEmailPersistenceService gmailEmailPersistenceService;
    private final GmailSyncPlanner gmailSyncPlanner;
    private final GmailSyncLockManager gmailSyncLockManager;

    public GmailSyncService(
            MailboxResourceResolver mailboxResourceResolver,
            MailAccountRepository mailAccountRepository,
            EmailMessageRepository emailMessageRepository,
            GmailClient gmailClient,
            TransactionTemplate transactionTemplate,
            MailboxDeduplicationService mailboxDeduplicationService,
            GmailEmailPersistenceService gmailEmailPersistenceService,
            GmailSyncPlanner gmailSyncPlanner,
            GmailSyncLockManager gmailSyncLockManager
    ) {
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.mailAccountRepository = mailAccountRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.gmailClient = gmailClient;
        this.transactionTemplate = transactionTemplate;
        this.mailboxDeduplicationService = mailboxDeduplicationService;
        this.gmailEmailPersistenceService = gmailEmailPersistenceService;
        this.gmailSyncPlanner = gmailSyncPlanner;
        this.gmailSyncLockManager = gmailSyncLockManager;
    }

    public MailSyncResponse sync(String userId, String mailAccountId, int limitParam) {
        try (GmailSyncLockManager.SyncLock ignored = gmailSyncLockManager.acquire(mailAccountId)) {
            return syncLocked(userId, mailAccountId, limitParam);
        }
    }

    private MailSyncResponse syncLocked(String userId, String mailAccountId, int limitParam) {
        MailAccount account = mailboxResourceResolver.getRequiredMailAccount(userId, mailAccountId);

        GmailSyncPlan syncPlan = gmailSyncPlanner.plan(account, limitParam);
        GmailSyncStats stats = new GmailSyncStats(mailboxDeduplicationService.deduplicateUserEmails(userId));
        String pageToken = null;

        do {
            int pageSize = Math.min(GMAIL_PAGE_SIZE, syncPlan.requestedLimit() - stats.fetched());
            if (pageSize <= 0) {
                break;
            }

            GmailListResponse listResponse = gmailClient.listMessages(
                    syncPlan.accessToken(),
                    pageSize,
                    pageToken,
                    syncPlan.gmailQuery()
            );
            List<GmailMessageRef> refs = listResponse.messages() == null ? List.of() : listResponse.messages();
            stats.recordFetched(refs.size());

            for (GmailMessageRef ref : refs) {
                if (ref.id() == null || ref.id().isBlank()) {
                    stats.recordSkipped();
                    continue;
                }
                if (isAlreadySynced(userId, account, ref.id())) {
                    stats.recordSkipped();
                    continue;
                }

                GmailMessageResponse gmailMessage = gmailClient.getMessage(syncPlan.accessToken(), ref.id());
                GmailEmailPersistenceService.PersistEmailResult persistResult = gmailEmailPersistenceService.persistEmail(
                        userId,
                        account,
                        ref.id(),
                        gmailMessage
                );
                if (!persistResult.inserted()) {
                    stats.recordSkipped();
                    continue;
                }

                if (persistResult.analysisEligible()) {
                    stats.recordAnalysisRequested();
                } else {
                    stats.recordAnalysisSkipped();
                }
                stats.recordInserted();
            }

            pageToken = listResponse.nextPageToken();
        } while (pageToken != null && !pageToken.isBlank() && stats.fetched() < syncPlan.requestedLimit());

        LocalDateTime syncedAt = LocalDateTime.now();
        markSynced(account.getId(), syncedAt);
        stats.recordSkipped(mailboxDeduplicationService.deduplicateUserEmails(userId));
        return new MailSyncResponse(
                account.getId(),
                syncPlan.syncAll() ? stats.fetched() : syncPlan.requestedLimit(),
                stats.fetched(),
                stats.inserted(),
                stats.skipped(),
                stats.analysisRequested(),
                stats.analysisCompleted(),
                stats.analysisSkipped(),
                syncedAt
        );
    }

    private boolean isAlreadySynced(String userId, MailAccount account, String externalMessageId) {
        return emailMessageRepository.findByMailAccount_IdAndExternalMessageId(account.getId(), externalMessageId).isPresent()
                || emailMessageRepository.existsByUserProviderAccountEmailAndExternalMessageId(
                userId,
                account.getProvider(),
                account.getAccountEmail(),
                externalMessageId
        );
    }

    private void markSynced(String mailAccountId, LocalDateTime syncedAt) {
        transactionTemplate.executeWithoutResult(status ->
                mailAccountRepository.findById(mailAccountId).ifPresent(account -> account.markSynced(syncedAt))
        );
    }

}
