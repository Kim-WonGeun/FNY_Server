package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobFactory {

    private static final String EMAIL_ANALYSIS_JOB_TYPE = "EMAIL_ANALYSIS";

    private final IdGenerator idGenerator;

    public AnalysisJobFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public AnalysisJob emailAnalysisJob(EmailMessage email, int priority) {
        return new AnalysisJob(
                idGenerator.generate("JOB"),
                email,
                EMAIL_ANALYSIS_JOB_TYPE,
                AnalysisJob.STATUS_PENDING,
                priority
        );
    }
}
