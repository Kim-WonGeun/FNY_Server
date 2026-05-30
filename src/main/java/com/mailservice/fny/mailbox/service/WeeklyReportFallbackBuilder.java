package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportContent;
import com.mailservice.fny.mailbox.dto.WeeklyReportThreadItem;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportFallbackBuilder {

    private final WeeklyReportTextAnalyzer weeklyReportTextAnalyzer;

    public WeeklyReportFallbackBuilder(WeeklyReportTextAnalyzer weeklyReportTextAnalyzer) {
        this.weeklyReportTextAnalyzer = weeklyReportTextAnalyzer;
    }

    public WeeklyReportContent build(List<EmailMessage> emails, String periodLabel) {
        StringBuilder corpus = new StringBuilder();
        List<WeeklyReportThreadItem> threads = new ArrayList<>();

        for (EmailMessage email : emails) {
            String subject = email.getSubject() != null ? email.getSubject() : "(제목 없음)";
            String body = weeklyReportTextAnalyzer.combinedBody(email);
            String text = subject + "\n" + body;
            corpus.append(text).append('\n');
            String oneLiner = String.join(", ", weeklyReportTextAnalyzer.extractKeywords(text, 6));
            WeeklyReportSection section = weeklyReportTextAnalyzer.classifyReportSection(text);
            threads.add(new WeeklyReportThreadItem(
                    email.getId(),
                    subject,
                    section.label() + ": " + (oneLiner.isBlank() ? weeklyReportTextAnalyzer.excerpt(body, 80) : oneLiner),
                    email.getFromEmail(),
                    email.getReceivedAt().toString(),
                    section.code(),
                    weeklyReportTextAnalyzer.evidenceSentence(email, section)
            ));
        }

        List<String> keywords = weeklyReportTextAnalyzer.extractKeywords(corpus.toString(), 24);
        List<String> issueKeywords = keywordGroup(keywords, Set.of("장애", "보안", "계약", "정산", "세금계산서", "예산", "결제", "인프라"));
        List<String> workKeywords = keywordGroup(keywords, Set.of("승인", "검토", "확인", "요청", "일정", "회의록", "권한", "정책"));
        List<String> topicKeywords = keywordGroup(keywords, Set.of("고객", "미팅", "배포", "QA", "릴리즈", "파트너", "마케팅", "데이터", "운영", "성과", "보고", "공유"));
        String executive = periodLabel + " 수신 메일 " + emails.size() + "건을 바탕으로 주간보고 초안을 구성했습니다.";

        return new WeeklyReportContent(
                "FALLBACK",
                executive,
                List.of(
                        reportLine("금주실적", !topicKeywords.isEmpty() ? topicKeywords : keywords, "금주실적: 선택 기간 메일에서 주요 완료 내용을 추가 확인해 주세요."),
                        reportLine("진행사항", !workKeywords.isEmpty() ? workKeywords : keywords, "진행사항: 검토 중인 업무를 메일 원문 기준으로 보강해 주세요.")
                ),
                List.of(reportLine("특이사항", issueKeywords, "특이사항: 별도 이슈로 분류할 키워드가 뚜렷하지 않습니다.")),
                List.of(reportLine("확인필요", workKeywords, "확인필요: 추가 확인이 필요한 항목이 뚜렷하지 않습니다.")),
                List.of(reportLine("차주계획", mergeLimited(workKeywords, topicKeywords, 8), "차주계획: 금주 메일 흐름을 바탕으로 다음 주 계획을 보강해 주세요.")),
                threads.stream().limit(40).toList(),
                "server-fallback",
                "weekly-report-v1"
        );
    }

    private static List<String> keywordGroup(List<String> keywords, Set<String> candidates) {
        return keywords.stream()
                .filter(candidates::contains)
                .limit(10)
                .toList();
    }

    private static String reportLine(String prefix, List<String> keywords, String fallback) {
        List<String> selected = keywords.stream().limit(5).toList();
        if (selected.isEmpty()) {
            return fallback;
        }
        return prefix + ": " + String.join(", ", selected) + " 관련 내용을 정리했습니다.";
    }

    private static List<String> mergeLimited(List<String> first, List<String> second, int limit) {
        List<String> merged = new ArrayList<>();
        merged.addAll(first);
        merged.addAll(second);
        return merged.stream().distinct().limit(limit).toList();
    }
}
