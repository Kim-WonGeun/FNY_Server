package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCandidateEvaluator {

    static final int ANALYSIS_WINDOW_DAYS = 30;
    static final int CANDIDATE_SCORE_THRESHOLD = 40;

    public AnalysisCandidateDecision evaluate(
            MailAccount account,
            EmailMessage email,
            List<String> labels,
            String toHeader
    ) {
        CandidateScore score = calculateScore(account, email, labels, toHeader);

        if (email.getReceivedAt().isBefore(LocalDateTime.now().minusDays(ANALYSIS_WINDOW_DAYS))) {
            return new AnalysisCandidateDecision(false, score.value(), score.reasonCodes(), "OLD_MAIL");
        }

        if (isAutomatedNoise(labels, email)) {
            return new AnalysisCandidateDecision(
                    false,
                    score.value(),
                    score.reasonCodes(),
                    "AUTOMATED_OR_PROMOTIONAL"
            );
        }

        boolean eligible = score.value() >= CANDIDATE_SCORE_THRESHOLD;
        return new AnalysisCandidateDecision(
                eligible,
                score.value(),
                score.reasonCodes(),
                eligible ? null : "LOW_SIGNAL"
        );
    }

    private CandidateScore calculateScore(
            MailAccount account,
            EmailMessage email,
            List<String> labels,
            String toHeader
    ) {
        int score = 0;
        List<String> reasonCodes = new ArrayList<>();
        String subject = nullToBlank(email.getSubject());
        String snippet = nullToBlank(email.getMessageSnippet());
        String body = nullToBlank(email.getBodyText());
        String content = (subject + " " + snippet + " " + body).toLowerCase(Locale.ROOT);
        String fromEmail = nullToBlank(email.getFromEmail()).toLowerCase(Locale.ROOT);
        String accountEmail = nullToBlank(account.getAccountEmail()).toLowerCase(Locale.ROOT);
        String normalizedToHeader = nullToBlank(toHeader).toLowerCase(Locale.ROOT);

        if (!email.isRead()) {
            score += 25;
            reasonCodes.add("UNREAD");
        }
        if (email.isStarred()) {
            score += 35;
            reasonCodes.add("STARRED");
        }
        if ("high".equalsIgnoreCase(nullToBlank(email.getImportanceHeader()))) {
            score += 25;
            reasonCodes.add("IMPORTANT_HEADER");
        }
        if (email.isHasAttachment()) {
            score += 15;
            reasonCodes.add("HAS_ATTACHMENT");
        }
        if (!email.getReceivedAt().isBefore(LocalDateTime.now().minusDays(3))) {
            score += 15;
            reasonCodes.add("RECENT_3_DAYS");
        }
        if (normalizedToHeader.contains(accountEmail)) {
            score += 20;
            reasonCodes.add("DIRECT_TO_ME");
        }
        if (AnalysisCandidateRules.containsActionKeyword(content)) {
            score += 35;
            reasonCodes.add("ACTION_KEYWORD");
        }
        if (AnalysisCandidateRules.containsMeetingKeyword(content)) {
            score += 20;
            reasonCodes.add("MEETING_KEYWORD");
        }
        if (AnalysisCandidateRules.containsIncidentKeyword(content)) {
            score += 35;
            reasonCodes.add("INCIDENT_KEYWORD");
        }
        if (AnalysisCandidateRules.hasLowValueLabel(labels)) {
            score -= 45;
            reasonCodes.add("LOW_VALUE_CATEGORY");
        }
        if (AnalysisCandidateRules.isAutoSender(fromEmail)) {
            score -= 30;
            reasonCodes.add("AUTO_SENDER");
        }
        if (AnalysisCandidateRules.containsLowValueContentKeyword(content)) {
            score -= 35;
            reasonCodes.add("LOW_VALUE_CONTENT");
        }
        if (fromEmail.equals(accountEmail)) {
            score -= 15;
            reasonCodes.add("SELF_SENT");
        }

        return new CandidateScore(score, reasonCodes);
    }

    private boolean isAutomatedNoise(List<String> labels, EmailMessage email) {
        String subject = nullToBlank(email.getSubject());
        String snippet = nullToBlank(email.getMessageSnippet());
        String body = nullToBlank(email.getBodyText());
        String content = (subject + " " + snippet + " " + body).toLowerCase(Locale.ROOT);
        String fromEmail = nullToBlank(email.getFromEmail()).toLowerCase(Locale.ROOT);

        boolean lowValueCategory = AnalysisCandidateRules.hasLowValueLabel(labels);
        boolean autoSender = AnalysisCandidateRules.isAutoSender(fromEmail);
        boolean lowValueContent = AnalysisCandidateRules.containsLowValueContentKeyword(content);
        boolean hasActionSignal = AnalysisCandidateRules.containsActionKeyword(content)
                || AnalysisCandidateRules.containsMeetingKeyword(content)
                || AnalysisCandidateRules.containsIncidentKeyword(content)
                || email.isStarred()
                || "high".equalsIgnoreCase(nullToBlank(email.getImportanceHeader()));

        return (lowValueCategory || autoSender || lowValueContent) && !hasActionSignal;
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    public record CandidateScore(int value, List<String> reasonCodes) {
    }

    public record AnalysisCandidateDecision(
            boolean eligible,
            int score,
            List<String> reasonCodes,
            String skippedReason
    ) {
    }
}
