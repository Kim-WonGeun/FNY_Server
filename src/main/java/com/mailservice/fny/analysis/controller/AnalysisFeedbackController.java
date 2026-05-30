package com.mailservice.fny.analysis.controller;

import com.mailservice.fny.analysis.dto.AnalysisFeedbackRequest;
import com.mailservice.fny.analysis.dto.AnalysisFeedbackResponse;
import com.mailservice.fny.analysis.service.AnalysisFeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-analyses")
public class AnalysisFeedbackController {

    private final AnalysisFeedbackService analysisFeedbackService;

    public AnalysisFeedbackController(AnalysisFeedbackService analysisFeedbackService) {
        this.analysisFeedbackService = analysisFeedbackService;
    }

    @PutMapping("/{analysisId}/feedbacks/{userId}")
    public AnalysisFeedbackResponse saveFeedback(
            @PathVariable String analysisId,
            @PathVariable String userId,
            @Valid @RequestBody AnalysisFeedbackRequest request
    ) {
        return analysisFeedbackService.saveFeedback(analysisId, userId, request);
    }
}
