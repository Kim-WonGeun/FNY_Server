package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.dto.AnalysisActionItemRequest;
import com.mailservice.fny.analysis.dto.AnalysisResultRequest;
import com.mailservice.fny.analysis.entity.EmailActionItem;
import com.mailservice.fny.analysis.entity.EmailAnalysis;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnalysisResultMapper {

    private final IdGenerator idGenerator;

    public AnalysisResultMapper(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    EmailAnalysis toAnalysis(EmailMessage email, int nextVersion, AnalysisResultRequest request) {
        return new EmailAnalysis(
                idGenerator.generate("ANL"),
                email,
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
                request.deadlineText(),
                request.timeSensitivity(),
                request.requiresAction(),
                request.userTaskSummary(),
                normalizePriorityReasonCodes(request),
                request.suggestedAction(),
                request.reasoning()
        );
    }

    EmailActionItem toActionItem(EmailAnalysis analysis, AnalysisActionItemRequest item) {
        return new EmailActionItem(
                idGenerator.generate("ACT"),
                analysis,
                item.actionText(),
                item.actionType(),
                item.priorityLevel(),
                item.dueAt()
        );
    }

    private static String normalizePriorityReasonCodes(AnalysisResultRequest request) {
        if (request.priorityReasonCodes() == null) {
            return null;
        }
        return request.priorityReasonCodes().stream()
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.joining(","));
    }
}
