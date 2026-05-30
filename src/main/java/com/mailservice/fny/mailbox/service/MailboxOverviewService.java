package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.dto.InboxEmailSummary;
import com.mailservice.fny.mailbox.dto.MailboxOverviewResponse;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailboxOverviewService {

    private static final Set<String> OPEN_ANALYSIS_JOB_STATUSES = Set.of(
            AnalysisJob.STATUS_PENDING,
            AnalysisJob.STATUS_RUNNING,
            AnalysisJob.STATUS_WAITING_AGENT
    );

    private final AppUserRepository appUserRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final MailboxAttentionPolicy mailboxAttentionPolicy;

    public MailboxOverviewService(
            AppUserRepository appUserRepository,
            EmailMessageRepository emailMessageRepository,
            AnalysisJobRepository analysisJobRepository,
            MailboxAttentionPolicy mailboxAttentionPolicy
    ) {
        this.appUserRepository = appUserRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.mailboxAttentionPolicy = mailboxAttentionPolicy;
    }

    public MailboxOverviewResponse getOverview(String userId) {
        ensureUserExists(userId);
        List<InboxEmailSummary> inboxEmails = emailMessageRepository.findInboxByUserId(userId);
        List<EmailListResponse> spotlightEmails = inboxEmails.stream()
                .filter(mailboxAttentionPolicy::isSpotlightCandidate)
                .limit(5)
                .map(EmailListResponse::from)
                .toList();

        return new MailboxOverviewResponse(
                userId,
                emailMessageRepository.countByMailAccountUserId(userId),
                emailMessageRepository.countByMailAccountUserIdAndIsReadFalse(userId),
                inboxEmails.stream()
                        .filter(mailboxAttentionPolicy::isOpenAttention)
                        .filter(email -> Boolean.TRUE.equals(email.needsReply()))
                        .count(),
                inboxEmails.stream()
                        .filter(mailboxAttentionPolicy::isOpenAttention)
                        .filter(email -> mailboxAttentionPolicy.isHighPriority(email.priorityLevel()))
                        .count(),
                analysisJobRepository.countByEmailMailAccountUserIdAndStatusIn(userId, OPEN_ANALYSIS_JOB_STATUSES),
                spotlightEmails
        );
    }

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }

}
