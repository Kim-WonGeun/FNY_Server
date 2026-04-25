package com.mailservice.fny.analysis.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.Objects;

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
                Objects.toString(email.getSubject(), ""),
                resolveBody(email),
                Objects.toString(email.getFromEmail(), ""),
                email.getReceivedAt().toString(),
                "ko"
        );
    }

    private static String resolveBody(EmailMessage email) {
        if (email.getBodyText() != null && !email.getBodyText().isBlank()) {
            return email.getBodyText();
        }
        return Objects.toString(email.getMessageSnippet(), "");
    }
}
