package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.entity.PromptTemplate;
import com.mailservice.fny.analysis.infrastructure.AgentPromptTemplateRequest;
import com.mailservice.fny.analysis.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PromptTemplateService {

    private static final String EMAIL_ANALYSIS_PROMPT_CODE = "EMAIL_ANALYSIS";
    private static final String EMAIL_ANALYSIS_PROMPT_TYPE = "ANALYSIS";
    private static final String WEEKLY_REPORT_PROMPT_CODE = "WEEKLY_REPORT";
    private static final String WEEKLY_REPORT_PROMPT_TYPE = "REPORT";

    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateService(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    public AgentPromptTemplateRequest resolveEmailAnalysisPrompt() {
        return resolvePromptTemplate(
                EMAIL_ANALYSIS_PROMPT_CODE,
                EMAIL_ANALYSIS_PROMPT_TYPE,
                fallbackEmailAnalysisPrompt()
        );
    }

    public AgentPromptTemplateRequest resolveWeeklyReportPrompt() {
        return resolvePromptTemplate(
                WEEKLY_REPORT_PROMPT_CODE,
                WEEKLY_REPORT_PROMPT_TYPE,
                fallbackWeeklyReportPrompt()
        );
    }

    private AgentPromptTemplateRequest resolvePromptTemplate(
            String promptCode,
            String promptType,
            AgentPromptTemplateRequest fallback
    ) {
        return promptTemplateRepository
                .findFirstByPromptCodeAndPromptTypeAndActiveTrueOrderByVersionDesc(promptCode, promptType)
                .map(PromptTemplateService::toAgentPrompt)
                .orElse(fallback);
    }

    private static AgentPromptTemplateRequest toAgentPrompt(PromptTemplate prompt) {
        return new AgentPromptTemplateRequest(
                prompt.getPromptCode(),
                prompt.getVersion(),
                prompt.getModelName(),
                prompt.getRoleContent(),
                prompt.getPolicyContent(),
                prompt.getGuideContent(),
                prompt.getOutputContent()
        );
    }

    private static AgentPromptTemplateRequest fallbackEmailAnalysisPrompt() {
        return new AgentPromptTemplateRequest(
                EMAIL_ANALYSIS_PROMPT_CODE,
                1,
                "gpt-5.4-mini",
                "너는 업무 메일을 구조화된 분석 결과로 정리하는 비서다.",
                "메일 원문에 없는 사실을 만들지 말고, 근거가 약하면 보수적으로 판단한다.",
                "긴급도, 중요도, 회신 필요 여부, 마감 여부, 추천 액션을 표준 코드와 JSON 필드에 맞춰 반환한다.",
                "priority_reason_codes는 NEEDS_REPLY, HAS_DEADLINE, URGENT_KEYWORD, DIRECT_TO_ME, IMPORTANT_HEADER, ATTACHMENT, FINANCE_RELATED, MEETING_RELATED, APPROVAL_REQUIRED, CUSTOMER_OR_CONTRACT, NO_STRONG_SIGNAL 중에서 사용한다. action_type은 REPLY, REVIEW, APPROVE, SCHEDULE, PAYMENT, FOLLOW_UP, ARCHIVE 중에서 사용한다. 지정된 JSON만 반환한다."
        );
    }

    private static AgentPromptTemplateRequest fallbackWeeklyReportPrompt() {
        return new AgentPromptTemplateRequest(
                WEEKLY_REPORT_PROMPT_CODE,
                1,
                "gpt-5.4-mini",
                "너는 업무 메일을 근거로 주간보고 초안을 작성하는 비서다.",
                "메일 원문에 없는 사실을 만들지 말고, 불확실한 내용은 확인필요로 분류한다.",
                "금주실적, 진행사항, 특이사항, 확인필요, 차주계획을 업무 보고 문체로 작성한다.",
                "executive_summary, highlights, risks_blockers, pending_decisions, next_week_suggestions, thread_summaries JSON만 반환한다."
        );
    }
}
