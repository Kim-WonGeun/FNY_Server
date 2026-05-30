package com.mailservice.fny.mailbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalysisCandidateEvaluatorTest {

    private final AnalysisCandidateEvaluator evaluator = new AnalysisCandidateEvaluator();

    @Test
    void keepsRecentActionMailAsAnalysisCandidate() {
        MailAccount account = mailAccount("user1@test.com");
        EmailMessage email = email(
                account,
                "계약 검토 요청",
                "첨부 계약서 검토 후 오늘 중 회신 부탁드립니다.",
                "legal@test.com",
                false,
                true,
                true,
                "high",
                LocalDateTime.now().minusHours(2)
        );

        AnalysisCandidateEvaluator.AnalysisCandidateDecision decision = evaluator.evaluate(
                account,
                email,
                List.of("INBOX", "IMPORTANT"),
                "user1@test.com"
        );

        assertThat(decision.eligible()).isTrue();
        assertThat(decision.skippedReason()).isNull();
        assertThat(decision.reasonCodes()).contains("DIRECT_TO_ME", "ACTION_KEYWORD", "HAS_ATTACHMENT");
    }

    @Test
    void excludesOldMailEvenIfScoreIsHigh() {
        MailAccount account = mailAccount("user1@test.com");
        EmailMessage email = email(
                account,
                "긴급 장애 확인 요청",
                "긴급 장애 원인 파악 후 회신 부탁드립니다.",
                "devlead@test.com",
                false,
                false,
                false,
                "high",
                LocalDateTime.now().minusDays(31)
        );

        AnalysisCandidateEvaluator.AnalysisCandidateDecision decision = evaluator.evaluate(
                account,
                email,
                List.of("INBOX"),
                "user1@test.com"
        );

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.skippedReason()).isEqualTo("OLD_MAIL");
    }

    @Test
    void excludesAutomatedPromotionalMailWithoutActionSignals() {
        MailAccount account = mailAccount("user1@test.com");
        EmailMessage email = email(
                account,
                "4월 뉴스레터",
                "이번 주 프로모션과 할인 혜택, 추천 상품 소식을 안내드립니다.",
                "newsletter@service.com",
                false,
                false,
                false,
                "normal",
                LocalDateTime.now().minusHours(5)
        );

        AnalysisCandidateEvaluator.AnalysisCandidateDecision decision = evaluator.evaluate(
                account,
                email,
                List.of("CATEGORY_PROMOTIONS"),
                "user1@test.com"
        );

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.skippedReason()).isEqualTo("AUTOMATED_OR_PROMOTIONAL");
        assertThat(decision.reasonCodes()).contains("LOW_VALUE_CATEGORY", "LOW_VALUE_CONTENT");
    }

    @Test
    void keepsSecurityOrAlertMailWhenItHasStrongIncidentSignals() {
        MailAccount account = mailAccount("user1@test.com");
        EmailMessage email = email(
                account,
                "긴급 보안 알림",
                "운영 계정 로그인 실패가 반복되어 즉시 확인이 필요합니다.",
                "alert@security.test.com",
                false,
                false,
                false,
                "high",
                LocalDateTime.now().minusMinutes(30)
        );

        AnalysisCandidateEvaluator.AnalysisCandidateDecision decision = evaluator.evaluate(
                account,
                email,
                List.of("INBOX"),
                "user1@test.com"
        );

        assertThat(decision.eligible()).isTrue();
        assertThat(decision.skippedReason()).isNull();
        assertThat(decision.reasonCodes()).contains("INCIDENT_KEYWORD", "IMPORTANT_HEADER");
    }

    @Test
    void excludesLowSignalMailWithinAnalysisWindow() {
        MailAccount account = mailAccount("user1@test.com");
        EmailMessage email = email(
                account,
                "팀 소식 안내",
                "이번 주 팀 소식과 일반 안내 사항을 정리했습니다.",
                "pm@test.com",
                true,
                false,
                false,
                "normal",
                LocalDateTime.now().minusDays(5)
        );

        AnalysisCandidateEvaluator.AnalysisCandidateDecision decision = evaluator.evaluate(
                account,
                email,
                List.of("INBOX"),
                "team@test.com"
        );

        assertThat(decision.eligible()).isFalse();
        assertThat(decision.skippedReason()).isEqualTo("LOW_SIGNAL");
    }

    private static MailAccount mailAccount(String accountEmail) {
        return new MailAccount(
                "MAC_TEST_000001",
                null,
                "GOOGLE",
                "google_test_account",
                accountEmail,
                "테스트 계정",
                true
        );
    }

    private static EmailMessage email(
            MailAccount account,
            String subject,
            String body,
            String fromEmail,
            boolean isRead,
            boolean hasAttachment,
            boolean isStarred,
            String importanceHeader,
            LocalDateTime receivedAt
    ) {
        return new EmailMessage(
                "EML_TEST_000001",
                account,
                "external-message-id",
                "external-thread-id",
                "<internet@test.com>",
                subject,
                body,
                "<p>" + body + "</p>",
                body,
                "발신자",
                fromEmail,
                receivedAt,
                receivedAt,
                isRead,
                isStarred,
                hasAttachment,
                importanceHeader,
                "{\"provider\":\"gmail\"}"
        );
    }
}
