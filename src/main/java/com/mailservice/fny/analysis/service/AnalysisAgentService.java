package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.infrastructure.AgentAnalysisException;
import com.mailservice.fny.analysis.infrastructure.AgentAnalysisClient;
import com.mailservice.fny.analysis.infrastructure.AgentAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalysisAgentService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisAgentService.class);

    private final AgentAnalysisClient agentAnalysisClient;
    private final AnalysisResultService analysisResultService;
    private final boolean enabled;

    public AnalysisAgentService(
            AgentAnalysisClient agentAnalysisClient,
            AnalysisResultService analysisResultService,
            @Value("${fny.agent.enabled:false}") boolean enabled
    ) {
        this.agentAnalysisClient = agentAnalysisClient;
        this.analysisResultService = analysisResultService;
        this.enabled = enabled;
    }

    public boolean analyzeAndStore(AnalysisJob job) {
        if (!enabled) {
            return false;
        }

        try {
            AgentAnalysisResponse response = agentAnalysisClient.analyze(job.getEmail());
            if (response == null) {
                return false;
            }
            analysisResultService.saveResult(job.getId(), response.toRequest());
            return true;
        } catch (AgentAnalysisException exception) {
            log.warn("Agent analysis request failed. jobId={}, status={}, body={}",
                    job.getId(), exception.getStatusCode(), exception.getResponseBody());
            return false;
        } catch (RuntimeException exception) {
            log.warn("Agent analysis request failed. jobId={}, message={}", job.getId(), exception.getMessage());
            return false;
        }
    }
}
