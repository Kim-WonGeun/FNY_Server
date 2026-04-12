package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.dto.AnalysisActionItemRequest;
import com.mailservice.fny.analysis.dto.AnalysisResultRequest;
import com.mailservice.fny.analysis.dto.AnalysisResultSaveResponse;
import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.entity.EmailActionItem;
import com.mailservice.fny.analysis.entity.EmailAnalysis;
import com.mailservice.fny.analysis.exception.AnalysisJobNotFoundException;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.analysis.repository.EmailActionItemRepository;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.common.IdGenerator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnalysisResultService {

    private final AnalysisJobRepository analysisJobRepository;
    private final EmailAnalysisRepository emailAnalysisRepository;
    private final EmailActionItemRepository emailActionItemRepository;
    private final IdGenerator idGenerator;

    public AnalysisResultService(
            AnalysisJobRepository analysisJobRepository,
            EmailAnalysisRepository emailAnalysisRepository,
            EmailActionItemRepository emailActionItemRepository,
            IdGenerator idGenerator
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.emailActionItemRepository = emailActionItemRepository;
        this.idGenerator = idGenerator;
    }

    public AnalysisResultSaveResponse saveResult(String jobId, AnalysisResultRequest request) {
        AnalysisJob job = analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new AnalysisJobNotFoundException(jobId));

        if (!job.getEmail().getId().equals(request.emailId())) {
            throw new IllegalArgumentException("분석 작업의 메일 ID와 요청 메일 ID가 일치하지 않습니다.");
        }

        emailAnalysisRepository.findByEmailIdAndIsLatestTrue(request.emailId())
                .ifPresent(EmailAnalysis::markNotLatest);

        int nextVersion = emailAnalysisRepository.findTopByEmailIdOrderByAnalysisVersionDesc(request.emailId())
                .map(existing -> existing.getAnalysisVersion() + 1)
                .orElse(1);

        EmailAnalysis analysis = new EmailAnalysis(
                idGenerator.generate("ANL"),
                job.getEmail(),
                nextVersion,
                request.modelName(),
                request.promptVersion(),
                request.shortSummary(),
                request.detailedSummary(),
                request.category(),
                request.priorityLevel(),
                request.importanceScore(),
                request.urgencyScore(),
                request.confidenceScore(),
                request.needsReply(),
                request.hasDeadline(),
                request.deadlineAt(),
                request.suggestedAction(),
                request.reasoning()
        );

        emailAnalysisRepository.save(analysis);

        List<AnalysisActionItemRequest> actionItems = request.actionItems() == null ? List.of() : request.actionItems();
        actionItems.stream()
                .map(item -> new EmailActionItem(
                        idGenerator.generate("ACT"),
                        analysis,
                        item.actionText(),
                        item.actionType(),
                        item.priorityLevel(),
                        item.dueAt()
                ))
                .forEach(emailActionItemRepository::save);

        job.complete(request.modelName());

        return new AnalysisResultSaveResponse(
                job.getId(),
                request.emailId(),
                analysis.getId(),
                job.getStatus(),
                actionItems.size()
        );
    }
}
