package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.service.AnalysisAgentService;
import com.mailservice.fny.analysis.service.AnalysisJobFactory;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailboxAnalysisJobService {

    private final MailboxResourceResolver mailboxResourceResolver;
    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisAgentService analysisAgentService;
    private final AnalysisJobFactory analysisJobFactory;
    private final MailboxAnalysisJobResponseMapper analysisJobResponseMapper;
    private final MailboxAnalysisJobPriorityResolver analysisJobPriorityResolver;

    public MailboxAnalysisJobService(
            MailboxResourceResolver mailboxResourceResolver,
            AnalysisJobRepository analysisJobRepository,
            AnalysisAgentService analysisAgentService,
            AnalysisJobFactory analysisJobFactory,
            MailboxAnalysisJobResponseMapper analysisJobResponseMapper,
            MailboxAnalysisJobPriorityResolver analysisJobPriorityResolver
    ) {
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.analysisJobRepository = analysisJobRepository;
        this.analysisAgentService = analysisAgentService;
        this.analysisJobFactory = analysisJobFactory;
        this.analysisJobResponseMapper = analysisJobResponseMapper;
        this.analysisJobPriorityResolver = analysisJobPriorityResolver;
    }

    @Transactional
    public AnalysisJobCreateResponse queueAnalysisJob(String emailId) {
        EmailMessage email = mailboxResourceResolver.getRequiredEmail(emailId);

        AnalysisJob job = analysisJobFactory.emailAnalysisJob(email, analysisJobPriorityResolver.resolve(emailId));

        analysisJobRepository.save(job);
        boolean completed = analysisAgentService.analyzeAndStore(job);

        return analysisJobResponseMapper.toCreateResponse(job, completed);
    }
}
