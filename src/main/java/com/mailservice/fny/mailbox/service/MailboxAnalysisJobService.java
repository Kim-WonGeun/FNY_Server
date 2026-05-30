package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.analysis.service.AnalysisAgentService;
import com.mailservice.fny.analysis.service.AnalysisJobFactory;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailboxAnalysisJobService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailAnalysisRepository emailAnalysisRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisAgentService analysisAgentService;
    private final AnalysisJobFactory analysisJobFactory;

    public MailboxAnalysisJobService(
            EmailMessageRepository emailMessageRepository,
            EmailAnalysisRepository emailAnalysisRepository,
            AnalysisJobRepository analysisJobRepository,
            AnalysisAgentService analysisAgentService,
            AnalysisJobFactory analysisJobFactory
    ) {
        this.emailMessageRepository = emailMessageRepository;
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.analysisAgentService = analysisAgentService;
        this.analysisJobFactory = analysisJobFactory;
    }

    @Transactional
    public AnalysisJobCreateResponse queueAnalysisJob(String emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new MailboxNotFoundException("메일을 찾을 수 없습니다. id=" + emailId));

        AnalysisJob job = analysisJobFactory.emailAnalysisJob(email, resolvePriority(emailId));

        analysisJobRepository.save(job);
        boolean completed = analysisAgentService.analyzeAndStore(job);
        String message = completed ? "분석 작업이 완료되었습니다." : resolveAnalysisJobMessage(job);
        String status = completed ? AnalysisJob.STATUS_COMPLETED : job.getStatus();

        return new AnalysisJobCreateResponse(job.getId(), status, message);
    }

    private String resolveAnalysisJobMessage(AnalysisJob job) {
        return switch (job.getStatus()) {
            case AnalysisJob.STATUS_WAITING_AGENT -> "Agent 서버가 준비되면 분석할 수 있습니다.";
            case AnalysisJob.STATUS_FAILED -> job.getErrorMessage() == null
                    ? "분석 작업이 실패했습니다."
                    : job.getErrorMessage();
            case AnalysisJob.STATUS_RUNNING -> "분석 작업이 진행 중입니다.";
            default -> "분석 작업이 큐에 등록되었습니다.";
        };
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
