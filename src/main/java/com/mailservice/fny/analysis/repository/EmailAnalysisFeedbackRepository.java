package com.mailservice.fny.analysis.repository;

import com.mailservice.fny.analysis.entity.EmailAnalysisFeedback;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAnalysisFeedbackRepository extends JpaRepository<EmailAnalysisFeedback, String> {

    Optional<EmailAnalysisFeedback> findByAnalysisIdAndUserId(String analysisId, String userId);
}
