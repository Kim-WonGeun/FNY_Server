package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import org.springframework.stereotype.Component;

@Component
public class MailboxAnalysisJobPriorityResolver {

    private static final int DEFAULT_PRIORITY = 5;

    private final EmailAnalysisRepository emailAnalysisRepository;

    public MailboxAnalysisJobPriorityResolver(EmailAnalysisRepository emailAnalysisRepository) {
        this.emailAnalysisRepository = emailAnalysisRepository;
    }

    public int resolve(String emailId) {
        return emailAnalysisRepository.findByEmailIdAndIsLatestTrue(emailId)
                .map(analysis -> switch (analysis.getPriorityLevel()) {
                    case "P1" -> 1;
                    case "P2" -> 2;
                    case "P3" -> 3;
                    default -> DEFAULT_PRIORITY;
                })
                .orElse(DEFAULT_PRIORITY);
    }
}
