package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailservice.fny.mailbox.entity.EmailMessage;

public record AgentAnalysisRequest(
        @JsonProperty("email_id") String emailId,
        String subject,
        @JsonProperty("body_text") String bodyText,
        @JsonProperty("from_email") String fromEmail,
        @JsonProperty("received_at") String receivedAt,
        String language
) {

    public static AgentAnalysisRequest from(EmailMessage email) {
        return new AgentAnalysisRequest(
                email.getId(),
                email.getSubject(),
                email.getBodyText(),
                email.getFromEmail(),
                email.getReceivedAt().toString(),
                "ko"
        );
    }
}
