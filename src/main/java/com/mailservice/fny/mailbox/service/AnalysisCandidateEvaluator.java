package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCandidateEvaluator {

    static final int ANALYSIS_WINDOW_DAYS = 30;
    static final int CANDIDATE_SCORE_THRESHOLD = 40;

    private final AnalysisCandidateScorer analysisCandidateScorer;

    public AnalysisCandidateEvaluator(AnalysisCandidateScorer analysisCandidateScorer) {
        this.analysisCandidateScorer = analysisCandidateScorer;
    }

    public AnalysisCandidateDecision evaluate(
            MailAccount account,
            EmailMessage email,
            List<String> labels,
            String toHeader
    ) {
        CandidateScore score = analysisCandidateScorer.calculateScore(account, email, labels, toHeader);

        if (email.getReceivedAt().isBefore(LocalDateTime.now().minusDays(ANALYSIS_WINDOW_DAYS))) {
            return new AnalysisCandidateDecision(false, score.value(), score.reasonCodes(), "OLD_MAIL");
        }

        if (analysisCandidateScorer.isAutomatedNoise(labels, email)) {
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
