package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportTextAnalyzer {

    private static final List<String> WEEKLY_KEYWORDS = List.of(
            "승인", "고객", "미팅", "정산", "세금계산서", "배포", "QA", "계약", "장애", "보안",
            "예산", "릴리즈", "파트너", "마케팅", "데이터", "인프라", "결제", "운영", "정책", "성과",
            "회의록", "일정", "검토", "확인", "요청", "보고", "공유", "권한", "알림"
    );
    private static final Set<String> WEEKLY_STOPWORDS = Set.of(
            "안녕하세요", "부탁드립니다", "공유드립니다", "확인해", "주세요", "이번", "다음", "오늘",
            "관련", "필요합니다", "있습니다", "합니다"
    );

    String combinedBody(EmailMessage email) {
        String snippet = email.getMessageSnippet();
        String body = email.getBodyText();
        if (body != null && !body.isBlank()) {
            return body;
        }
        return snippet != null ? snippet : "";
    }

    String excerpt(String text, int max) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.strip().replaceAll("\\s+", " ");
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max) + "…";
    }

    List<String> extractKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        for (String keyword : WEEKLY_KEYWORDS) {
            int count = countOccurrences(normalized, keyword.toLowerCase());
            if (count > 0) {
                scores.merge(keyword, count * 3, Integer::sum);
            }
        }

        for (String token : text.split("[^가-힣A-Za-z0-9]+")) {
            if (token.length() < 2 || WEEKLY_STOPWORDS.contains(token) || WEEKLY_STOPWORDS.contains(token.toLowerCase())) {
                continue;
            }
            scores.merge(token, 1, Integer::sum);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    WeeklyReportSection classifyReportSection(String text) {
        String normalized = nullToBlank(text).toLowerCase();
        if (containsAny(normalized, List.of("장애", "오류", "실패", "보안", "리스크", "지연", "긴급", "incident", "error", "failure"))) {
            return new WeeklyReportSection("ISSUE", "특이사항");
        }
        if (containsAny(normalized, List.of("확인", "승인", "검토", "회신", "요청", "결정", "정산", "계약", "approval", "review", "reply"))) {
            return new WeeklyReportSection("PENDING_DECISION", "확인필요");
        }
        if (containsAny(normalized, List.of("다음 주", "차주", "예정", "계획", "준비", "next week", "plan"))) {
            return new WeeklyReportSection("NEXT_PLAN", "차주계획");
        }
        if (containsAny(normalized, List.of("완료", "공유", "개선", "반영", "진행", "배포", "completed", "released"))) {
            return new WeeklyReportSection("HIGHLIGHT", "금주실적");
        }
        return new WeeklyReportSection("PROGRESS", "진행사항");
    }

    String evidenceSentence(EmailMessage email, WeeklyReportSection section) {
        String body = combinedBody(email);
        List<String> candidates = splitSentences(body);
        return candidates.stream()
                .filter(sentence -> hasSectionSignal(sentence, section))
                .findFirst()
                .or(() -> candidates.stream().findFirst())
                .map(sentence -> excerpt(sentence, 160))
                .orElseGet(() -> excerpt(Objects.toString(email.getMessageSnippet(), ""), 160));
    }

    private static int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = text.indexOf(keyword);
        while (index >= 0) {
            count++;
            index = text.indexOf(keyword, index + keyword.length());
        }
        return count;
    }

    private static boolean hasSectionSignal(String sentence, WeeklyReportSection section) {
        String normalized = nullToBlank(sentence).toLowerCase();
        return switch (section.code()) {
            case "ISSUE" -> containsAny(normalized, List.of("장애", "오류", "실패", "보안", "리스크", "지연", "긴급"));
            case "PENDING_DECISION" -> containsAny(normalized, List.of("확인", "승인", "검토", "회신", "요청", "결정", "정산", "계약"));
            case "NEXT_PLAN" -> containsAny(normalized, List.of("다음 주", "차주", "예정", "계획", "준비"));
            case "HIGHLIGHT" -> containsAny(normalized, List.of("완료", "공유", "개선", "반영", "진행", "배포"));
            default -> false;
        };
    }

    private static List<String> splitSentences(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.strip().split("(?<=[.!?。]|다\\.)\\s+")).stream()
                .map(String::strip)
                .filter(sentence -> !sentence.isBlank())
                .toList();
    }

    private static boolean containsAny(String value, List<String> keywords) {
        String lower = nullToBlank(value).toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}

record WeeklyReportSection(String code, String label) {
}
