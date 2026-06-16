package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.analysis.entity.AnalysisJob;
import org.springframework.stereotype.Component;

@Component
public class MailboxAnalysisJobResponseMapper {

    public AnalysisJobCreateResponse toCreateResponse(AnalysisJob job, boolean completed) {
        String status = completed ? AnalysisJob.STATUS_COMPLETED : job.getStatus();
        String message = completed ? "분석 작업이 완료되었습니다." : resolveMessage(job);
        return new AnalysisJobCreateResponse(job.getId(), status, message);
    }

    private String resolveMessage(AnalysisJob job) {
        return switch (job.getStatus()) {
            case AnalysisJob.STATUS_WAITING_AGENT -> "Agent 서버가 준비되면 분석할 수 있습니다.";
            case AnalysisJob.STATUS_FAILED -> job.getErrorMessage() == null
                    ? "분석 작업이 실패했습니다."
                    : job.getErrorMessage();
            case AnalysisJob.STATUS_RUNNING -> "분석 작업이 진행 중입니다.";
            default -> "분석 작업이 큐에 등록되었습니다.";
        };
    }
}
