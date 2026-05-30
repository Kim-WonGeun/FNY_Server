package com.mailservice.fny.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.infrastructure.AgentAnalysisClient;
import com.mailservice.fny.analysis.infrastructure.AgentAnalysisException;
import com.mailservice.fny.analysis.infrastructure.AgentPromptTemplateRequest;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import org.junit.jupiter.api.Test;

class AnalysisAgentServiceTest {

    private final AgentAnalysisClient agentAnalysisClient = mock(AgentAnalysisClient.class);
    private final AnalysisResultService analysisResultService = mock(AnalysisResultService.class);
    private final PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);

    @Test
    void disabledAgentMarksJobWaitingAgent() {
        AnalysisAgentService service = new AnalysisAgentService(
                agentAnalysisClient,
                analysisResultService,
                promptTemplateService,
                false
        );
        AnalysisJob job = new AnalysisJob("JOB_TEST", mock(EmailMessage.class), "EMAIL_ANALYSIS", "PENDING", 5);

        boolean completed = service.analyzeAndStore(job);

        assertThat(completed).isFalse();
        assertThat(job.getStatus()).isEqualTo(AnalysisJob.STATUS_WAITING_AGENT);
        assertThat(job.getErrorMessage()).contains("비활성화");
        assertThat(job.getStartedAt()).isNull();
        assertThat(job.getCompletedAt()).isNull();
    }

    @Test
    void agentFailureMarksJobFailedWithErrorMessage() {
        EmailMessage email = mock(EmailMessage.class);
        AnalysisJob job = new AnalysisJob("JOB_TEST", email, "EMAIL_ANALYSIS", "PENDING", 5);
        when(promptTemplateService.resolveEmailAnalysisPrompt()).thenReturn(fallbackPrompt());
        when(agentAnalysisClient.analyze(eq(email), any())).thenThrow(new AgentAnalysisException(500, "agent down"));
        AnalysisAgentService service = new AnalysisAgentService(
                agentAnalysisClient,
                analysisResultService,
                promptTemplateService,
                true
        );

        boolean completed = service.analyzeAndStore(job);

        assertThat(completed).isFalse();
        assertThat(job.getStatus()).isEqualTo(AnalysisJob.STATUS_FAILED);
        assertThat(job.getRetryCount()).isEqualTo(1);
        assertThat(job.getErrorMessage()).contains("status=500").contains("agent down");
        assertThat(job.getStartedAt()).isNotNull();
        assertThat(job.getCompletedAt()).isNotNull();
    }

    private static AgentPromptTemplateRequest fallbackPrompt() {
        return new AgentPromptTemplateRequest(
                "EMAIL_ANALYSIS",
                1,
                "gpt-5.4-mini",
                "role",
                "policy",
                "guide",
                "output"
        );
    }
}
