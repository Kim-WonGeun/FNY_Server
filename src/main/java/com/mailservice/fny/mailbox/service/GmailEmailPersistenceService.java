package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.service.AnalysisJobFactory;
import com.mailservice.fny.integration.gmail.GmailMessageResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.EmailRecipientRepository;
import com.mailservice.fny.mailbox.service.AnalysisCandidateEvaluator.AnalysisCandidateDecision;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class GmailEmailPersistenceService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final GmailMessageMapper gmailMessageMapper;
    private final AnalysisCandidateEvaluator analysisCandidateEvaluator;
    private final AnalysisJobFactory analysisJobFactory;
    private final TransactionTemplate transactionTemplate;

    public GmailEmailPersistenceService(
            EmailMessageRepository emailMessageRepository,
            EmailRecipientRepository emailRecipientRepository,
            AnalysisJobRepository analysisJobRepository,
            GmailMessageMapper gmailMessageMapper,
            AnalysisCandidateEvaluator analysisCandidateEvaluator,
            AnalysisJobFactory analysisJobFactory,
            TransactionTemplate transactionTemplate
    ) {
        this.emailMessageRepository = emailMessageRepository;
        this.emailRecipientRepository = emailRecipientRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.gmailMessageMapper = gmailMessageMapper;
        this.analysisCandidateEvaluator = analysisCandidateEvaluator;
        this.analysisJobFactory = analysisJobFactory;
        this.transactionTemplate = transactionTemplate;
    }

    public PersistEmailResult persistEmail(
            String userId,
            MailAccount account,
            String externalMessageId,
            GmailMessageResponse gmailMessage
    ) {
        try {
            return transactionTemplate.execute(status -> {
                if (isAlreadySynced(userId, account, externalMessageId)) {
                    return PersistEmailResult.skipped();
                }

                EmailMessage email = gmailMessageMapper.toEmailMessage(account, externalMessageId, gmailMessage);
                AnalysisCandidateDecision decision = evaluateAnalysisCandidate(account, email, gmailMessage);
                email.markAnalysisCandidate(
                        decision.eligible(),
                        decision.score(),
                        String.join(",", decision.reasonCodes()),
                        decision.skippedReason(),
                        LocalDateTime.now()
                );
                emailMessageRepository.save(email);
                emailRecipientRepository.saveAll(gmailMessageMapper.toRecipients(email, gmailMessage));
                if (decision.eligible()) {
                    queueAnalysis(email);
                }
                return PersistEmailResult.inserted(decision.eligible());
            });
        } catch (DataIntegrityViolationException exception) {
            return PersistEmailResult.skipped();
        }
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

    private AnalysisCandidateDecision evaluateAnalysisCandidate(
            MailAccount account,
            EmailMessage email,
            GmailMessageResponse message
    ) {
        return analysisCandidateEvaluator.evaluate(
                account,
                email,
                message.labelIds() == null ? List.of() : message.labelIds(),
                gmailMessageMapper.header(message.payload(), "To")
        );
    }

    private AnalysisJob queueAnalysis(EmailMessage email) {
        AnalysisJob job = analysisJobFactory.emailAnalysisJob(email, 5);
        return analysisJobRepository.save(job);
    }

    public record PersistEmailResult(boolean inserted, boolean analysisEligible) {

        static PersistEmailResult inserted(boolean analysisEligible) {
            return new PersistEmailResult(true, analysisEligible);
        }

        static PersistEmailResult skipped() {
            return new PersistEmailResult(false, false);
        }
    }
}
