package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.dto.AnalysisJobResponse;
import com.mailservice.fny.analysis.dto.EmailActionItemResponse;
import com.mailservice.fny.analysis.dto.EmailAnalysisResponse;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.repository.EmailActionItemRepository;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.dto.EmailLabelResponse;
import com.mailservice.fny.mailbox.dto.EmailRecipientResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.EmailLabelRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.EmailRecipientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MailboxDetailService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final EmailAnalysisRepository emailAnalysisRepository;
    private final EmailActionItemRepository emailActionItemRepository;
    private final EmailLabelRepository emailLabelRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final EmailDetailResponseMapper emailDetailResponseMapper;

    public MailboxDetailService(
            EmailMessageRepository emailMessageRepository,
            EmailRecipientRepository emailRecipientRepository,
            EmailAnalysisRepository emailAnalysisRepository,
            EmailActionItemRepository emailActionItemRepository,
            EmailLabelRepository emailLabelRepository,
            AnalysisJobRepository analysisJobRepository,
            EmailDetailResponseMapper emailDetailResponseMapper
    ) {
        this.emailMessageRepository = emailMessageRepository;
        this.emailRecipientRepository = emailRecipientRepository;
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.emailActionItemRepository = emailActionItemRepository;
        this.emailLabelRepository = emailLabelRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.emailDetailResponseMapper = emailDetailResponseMapper;
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

        return emailDetailResponseMapper.toResponse(
                email,
                analysis,
                recipients,
                labels,
                actionItems,
                analysisJobs
        );
    }

    public List<EmailAnalysisResponse> getEmailAnalyses(String emailId) {
        if (!emailMessageRepository.existsById(emailId)) {
            throw new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId);
        }
        return emailAnalysisRepository.findByEmailIdOrderByAnalysisVersionDesc(emailId).stream()
                .map(EmailAnalysisResponse::from)
                .toList();
    }
}
