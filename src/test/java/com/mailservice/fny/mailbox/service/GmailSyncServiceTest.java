package com.mailservice.fny.mailbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mailservice.fny.integration.gmail.GmailClient;
import com.mailservice.fny.integration.gmail.GmailListResponse;
import com.mailservice.fny.integration.gmail.GmailMessageRef;
import com.mailservice.fny.mailbox.dto.MailSyncResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionTemplate;

class GmailSyncServiceTest {

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final MailAccountRepository mailAccountRepository = mock(MailAccountRepository.class);
    private final EmailMessageRepository emailMessageRepository = mock(EmailMessageRepository.class);
    private final GmailClient gmailClient = mock(GmailClient.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final MailboxDeduplicationService deduplicationService = mock(MailboxDeduplicationService.class);
    private final GmailEmailPersistenceService gmailEmailPersistenceService = mock(GmailEmailPersistenceService.class);

    private GmailSyncService service;

    @BeforeEach
    void setUp() {
        service = new GmailSyncService(
                appUserRepository,
                mailAccountRepository,
                emailMessageRepository,
                gmailClient,
                transactionTemplate,
                deduplicationService,
                gmailEmailPersistenceService,
                new GmailSyncPlanner(),
                new GmailSyncLockManager()
        );
    }

    @Test
    void skipsExistingMessageBeforeFetchingFullGmailPayload() {
        MailAccount account = googleAccount();
        EmailMessage existingEmail = mock(EmailMessage.class);
        when(appUserRepository.existsById("USR_TEST")).thenReturn(true);
        when(mailAccountRepository.findByIdAndUser_Id("MAC_TEST", "USR_TEST")).thenReturn(Optional.of(account));
        when(deduplicationService.deduplicateUserEmails("USR_TEST")).thenReturn(0);
        when(gmailClient.listMessages(anyString(), anyInt(), eq(null), eq(null)))
                .thenReturn(new GmailListResponse(List.of(new GmailMessageRef("gmail-1", "thread-1")), null, 1));
        when(emailMessageRepository.findByMailAccount_IdAndExternalMessageId("MAC_TEST", "gmail-1"))
                .thenReturn(Optional.of(existingEmail));

        MailSyncResponse response = service.sync("USR_TEST", "MAC_TEST", 10);

        assertThat(response.fetchedCount()).isEqualTo(1);
        assertThat(response.insertedCount()).isZero();
        assertThat(response.skippedCount()).isEqualTo(1);
        verify(gmailClient, never()).getMessage(anyString(), anyString());
    }

    @Test
    void usesIncrementalGmailQueryAfterPreviousSync() {
        MailAccount account = googleAccount();
        LocalDateTime lastSyncedAt = LocalDateTime.of(2026, 5, 8, 9, 30);
        account.markSynced(lastSyncedAt);
        when(appUserRepository.existsById("USR_TEST")).thenReturn(true);
        when(mailAccountRepository.findByIdAndUser_Id("MAC_TEST", "USR_TEST")).thenReturn(Optional.of(account));
        when(deduplicationService.deduplicateUserEmails("USR_TEST")).thenReturn(0);
        when(gmailClient.listMessages(anyString(), anyInt(), eq(null), any()))
                .thenReturn(new GmailListResponse(List.of(), null, 0));

        service.sync("USR_TEST", "MAC_TEST", 0);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(gmailClient).listMessages(anyString(), anyInt(), eq(null), queryCaptor.capture());
        String expectedAfter = lastSyncedAt.minusDays(2).toLocalDate()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        assertThat(queryCaptor.getValue()).isEqualTo("after:" + expectedAfter);
    }

    private static MailAccount googleAccount() {
        MailAccount account = new MailAccount(
                "MAC_TEST",
                null,
                "GOOGLE",
                "google-test",
                "user@test.com",
                "테스트 계정",
                true
        );
        account.updateConnection(
                "user@test.com",
                "테스트 계정",
                "{plain-for-dev}access-token",
                "{plain-for-dev}refresh-token",
                LocalDateTime.now().plusHours(1)
        );
        return account;
    }
}
