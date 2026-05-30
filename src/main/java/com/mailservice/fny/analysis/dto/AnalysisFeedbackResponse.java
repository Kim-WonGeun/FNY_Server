package com.mailservice.fny.analysis.dto;

import com.mailservice.fny.analysis.entity.EmailAnalysisFeedback;
import java.time.LocalDateTime;

public record AnalysisFeedbackResponse(
        String id,
        String analysisId,
        String emailId,
        String userId,
        String feedbackType,
        String reasonCode,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AnalysisFeedbackResponse from(EmailAnalysisFeedback feedback) {
        return new AnalysisFeedbackResponse(
                feedback.getId(),
                feedback.getAnalysis().getId(),
                feedback.getEmail().getId(),
                feedback.getUser().getId(),
                feedback.getFeedbackType(),
                feedback.getReasonCode(),
                feedback.getMemo(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}
