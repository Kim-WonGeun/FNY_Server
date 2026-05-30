package com.mailservice.fny.analysis.service;

import com.mailservice.fny.analysis.dto.AnalysisFeedbackRequest;
import com.mailservice.fny.analysis.dto.AnalysisFeedbackResponse;
import com.mailservice.fny.analysis.entity.EmailAnalysis;
import com.mailservice.fny.analysis.entity.EmailAnalysisFeedback;
import com.mailservice.fny.analysis.repository.EmailAnalysisFeedbackRepository;
import com.mailservice.fny.analysis.repository.EmailAnalysisRepository;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisFeedbackService {

    private final EmailAnalysisRepository emailAnalysisRepository;
    private final EmailAnalysisFeedbackRepository feedbackRepository;
    private final AppUserRepository appUserRepository;
    private final IdGenerator idGenerator;

    public AnalysisFeedbackService(
            EmailAnalysisRepository emailAnalysisRepository,
            EmailAnalysisFeedbackRepository feedbackRepository,
            AppUserRepository appUserRepository,
            IdGenerator idGenerator
    ) {
        this.emailAnalysisRepository = emailAnalysisRepository;
        this.feedbackRepository = feedbackRepository;
        this.appUserRepository = appUserRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public AnalysisFeedbackResponse saveFeedback(String analysisId, String userId, AnalysisFeedbackRequest request) {
        EmailAnalysis analysis = emailAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new MailboxNotFoundException("분석 결과를 찾을 수 없습니다. id=" + analysisId));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        EmailAnalysisFeedback feedback = feedbackRepository.findByAnalysisIdAndUserId(analysisId, userId)
                .map(existing -> {
                    existing.update(request.feedbackType(), request.reasonCode(), request.memo());
                    return existing;
                })
                .orElseGet(() -> new EmailAnalysisFeedback(
                        idGenerator.generate("AFB"),
                        analysis,
                        analysis.getEmail(),
                        user,
                        request.feedbackType(),
                        request.reasonCode(),
                        request.memo()
                ));

        return AnalysisFeedbackResponse.from(feedbackRepository.save(feedback));
    }
}
