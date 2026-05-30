package com.mailservice.fny.mailbox.service;

import java.util.List;
import java.util.Locale;

final class AnalysisCandidateRules {

    private static final List<String> LOW_VALUE_LABELS = List.of(
            "CATEGORY_PROMOTIONS",
            "CATEGORY_SOCIAL",
            "CATEGORY_FORUMS"
    );
    private static final List<String> ACTION_KEYWORDS = List.of(
            "확인",
            "요청",
            "승인",
            "검토",
            "회신",
            "답변",
            "제출",
            "공유",
            "전달",
            "마감",
            "계약",
            "견적",
            "정산",
            "입금",
            "세금계산서",
            "please review",
            "approval",
            "request",
            "reply",
            "deadline",
            "due"
    );
    private static final List<String> MEETING_KEYWORDS = List.of(
            "회의",
            "미팅",
            "일정",
            "참석",
            "초대",
            "meeting",
            "schedule",
            "calendar",
            "invite"
    );
    private static final List<String> INCIDENT_KEYWORDS = List.of(
            "긴급",
            "장애",
            "오류",
            "실패",
            "이슈",
            "urgent",
            "incident",
            "error",
            "failure",
            "critical"
    );
    private static final List<String> AUTO_SENDER_KEYWORDS = List.of(
            "no-reply",
            "noreply",
            "notification",
            "newsletter",
            "marketing",
            "mailer-daemon",
            "bounce",
            "alert"
    );
    private static final List<String> LOW_VALUE_CONTENT_KEYWORDS = List.of(
            "뉴스레터",
            "프로모션",
            "이벤트",
            "광고",
            "할인",
            "인증번호",
            "보안 알림",
            "로그인 알림",
            "newsletter",
            "promotion",
            "sale",
            "verification code",
            "security alert",
            "login alert"
    );

    private AnalysisCandidateRules() {
    }

    static boolean hasLowValueLabel(List<String> labels) {
        return labels.stream().anyMatch(LOW_VALUE_LABELS::contains);
    }

    static boolean containsActionKeyword(String value) {
        return containsAny(value, ACTION_KEYWORDS);
    }

    static boolean containsMeetingKeyword(String value) {
        return containsAny(value, MEETING_KEYWORDS);
    }

    static boolean containsIncidentKeyword(String value) {
        return containsAny(value, INCIDENT_KEYWORDS);
    }

    static boolean isAutoSender(String value) {
        return containsAny(value, AUTO_SENDER_KEYWORDS);
    }

    static boolean containsLowValueContentKeyword(String value) {
        return containsAny(value, LOW_VALUE_CONTENT_KEYWORDS);
    }

    private static boolean containsAny(String value, List<String> keywords) {
        String lower = value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(lower::contains);
    }
}
