package com.mailservice.fny.analysis.controller;

import com.mailservice.fny.analysis.dto.AnalysisResultRequest;
import com.mailservice.fny.analysis.dto.AnalysisResultSaveResponse;
import com.mailservice.fny.analysis.service.AnalysisResultService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis-jobs")
public class AnalysisResultController {

    private final AnalysisResultService analysisResultService;

    public AnalysisResultController(AnalysisResultService analysisResultService) {
        this.analysisResultService = analysisResultService;
    }

    @PostMapping("/{jobId}/results")
    public AnalysisResultSaveResponse saveAnalysisResult(
            @PathVariable String jobId,
            @Valid @RequestBody AnalysisResultRequest request
    ) {
        return analysisResultService.saveResult(jobId, request);
    }
}
