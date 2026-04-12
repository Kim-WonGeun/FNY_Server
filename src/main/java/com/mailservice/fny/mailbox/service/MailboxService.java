package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.analysis.dto.AnalysisJobResponse;
import com.mailservice.fny.analysis.dto.EmailActionItemResponse;
import com.mailservice.fny.analysis.dto.EmailAnalysisResponse;
import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.repository.EmailActionItemRepository;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.analysis.service.AnalysisAgentService;
import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.dto.EmailLabelResponse;
import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.dto.EmailRecipientResponse;
import com.mailservice.fny.mailbox.dto.InboxEmailSummary;
import com.mailservice.fny.mailbox.dto.MailAccountResponse;
import com.mailservice.fny.mailbox.dto.MailboxOverviewResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailLabelRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.EmailRecipientRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailboxService {

    private static final Set<String> HIGH_PRIORITY_LEVELS = Set.of("P1", "P2");

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final EmailAnalysisRepository emailAnalysisRepository;
    private final EmailActionItemRepository emailActionItemRepository;
    private final EmailLabelRepository emailLabelRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisAgentService analysisAgentService;
    private final IdGenerator idGenerator;

    public MailboxService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository,
            EmailMessageRepository emailMessageRepository,
            EmailRecipientRepository emailRecipientRepository,
            EmailAnalysisRepository emailAnalysisRepository,
            EmailActionItemRepository emailActionItemRepository,
            EmailLabelRepository emailLabelRepository,
            AnalysisJobRepository analysisJobRepository,
            AnalysisAgentService analysisAgentService,
            IdGenerator idGenerator
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.emailRecipientRepository = emailRecipientRepository;
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.emailActionItemRepository = emailActionItemRepository;
        this.emailLabelRepository = emailLabelRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.analysisAgentService = analysisAgentService;
        this.idGenerator = idGenerator;
    }

    public List<MailAccountResponse> getMailAccounts(String userId) {
        ensureUserExists(userId);
        return mailAccountRepository.findByUserIdOrderByIsPrimaryDescCreatedAtAsc(userId).stream()
                .map(MailAccountResponse::from)
                .toList();
    }

    public List<EmailListResponse> getInbox(String userId, boolean unreadOnly, boolean highPriorityOnly, boolean needsReplyOnly) {
        ensureUserExists(userId);
        return emailMessageRepository.findInboxByUserId(userId).stream()
                .filter(email -> !unreadOnly || !email.isRead())
                .filter(email -> !highPriorityOnly || HIGH_PRIORITY_LEVELS.contains(email.priorityLevel()))
                .filter(email -> !needsReplyOnly || Boolean.TRUE.equals(email.needsReply()))
                .map(EmailListResponse::from)
                .toList();
    }

    public MailboxOverviewResponse getOverview(String userId) {
        ensureUserExists(userId);
        List<EmailListResponse> spotlightEmails = emailMessageRepository.findInboxByUserId(userId).stream()
                .filter(this::isSpotlightCandidate)
                .limit(5)
                .map(EmailListResponse::from)
                .toList();

        return new MailboxOverviewResponse(
                userId,
                emailMessageRepository.countByMailAccountUserId(userId),
                emailMessageRepository.countByMailAccountUserIdAndIsReadFalse(userId),
                emailAnalysisRepository.countByEmailMailAccountUserIdAndIsLatestTrueAndNeedsReplyTrue(userId),
                emailAnalysisRepository.countByEmailMailAccountUserIdAndIsLatestTrueAndPriorityLevelIn(userId, HIGH_PRIORITY_LEVELS),
                analysisJobRepository.countByEmailMailAccountUserIdAndStatus(userId, "PENDING"),
                spotlightEmails
        );
    }

    public EmailDetailResponse getEmailDetail(String emailId) {
        EmailMessage email = emailMessageRepository.findWithMailAccountById(emailId)
                .orElseThrow(() -> new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId));

        EmailAnalysisResponse analysis = emailAnalysisRepository.findByEmailIdAndIsLatestTrue(emailId)
                .map(EmailAnalysisResponse::from)
                .orElse(null);

        List<EmailRecipientResponse> recipients = emailRecipientRepository.findByEmailIdOrderByRecipientTypeAscCreatedAtAsc(emailId)
                .stream()
                .map(EmailRecipientResponse::from)
                .toList();

        List<EmailLabelResponse> labels = emailLabelRepository.findByEmailIdOrderByCreatedAtAsc(emailId).stream()
                .map(EmailLabelResponse::from)
                .toList();

        List<EmailActionItemResponse> actionItems = analysis == null
                ? List.of()
                : emailActionItemRepository.findByAnalysisIdOrderByCreatedAtAsc(analysis.id()).stream()
                .map(EmailActionItemResponse::from)
                .toList();

        List<AnalysisJobResponse> analysisJobs = analysisJobRepository.findByEmailIdOrderByCreatedAtDesc(emailId).stream()
                .map(AnalysisJobResponse::from)
                .toList();

        return new EmailDetailResponse(
                email.getId(),
                email.getMailAccount().getId(),
                email.getMailAccount().getAccountEmail(),
                email.getMailAccount().getProvider(),
                email.getExternalMessageId(),
                email.getExternalThreadId(),
                email.getInternetMessageId(),
                email.getSubject(),
                email.getBodyText(),
                email.getBodyHtml(),
                email.getMessageSnippet(),
                email.getFromName(),
                email.getFromEmail(),
                email.getReceivedAt(),
                email.getSentAt(),
                email.isRead(),
                email.isStarred(),
                email.isHasAttachment(),
                email.getImportanceHeader(),
                analysis,
                recipients,
                labels,
                actionItems,
                analysisJobs
        );
    }

    @Transactional
    public AnalysisJobCreateResponse queueAnalysisJob(String emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId));

        AnalysisJob job = new AnalysisJob(
                idGenerator.generate("JOB"),
                email,
                "EMAIL_ANALYSIS",
                "PENDING",
                resolvePriority(emailId)
        );

        analysisJobRepository.save(job);
        boolean completed = analysisAgentService.analyzeAndStore(job);
        String message = completed ? "분석 작업이 완료되었습니다." : "분석 작업이 큐에 등록되었습니다.";
        String status = completed ? "COMPLETED" : job.getStatus();

        return new AnalysisJobCreateResponse(job.getId(), status, message);
    }

    private void ensureUserExists(String userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }
    }

    private boolean isSpotlightCandidate(InboxEmailSummary email) {
        return HIGH_PRIORITY_LEVELS.contains(email.priorityLevel())
                || Boolean.TRUE.equals(email.needsReply())
                || !email.isRead()
                || email.isStarred();
    }

    private int resolvePriority(String emailId) {
        return emailAnalysisRepository.findByEmailIdAndIsLatestTrue(emailId)
                .map(analysis -> switch (analysis.getPriorityLevel()) {
                    case "P1" -> 1;
                    case "P2" -> 2;
                    case "P3" -> 3;
                    default -> 5;
                })
                .orElse(5);
    }
}
