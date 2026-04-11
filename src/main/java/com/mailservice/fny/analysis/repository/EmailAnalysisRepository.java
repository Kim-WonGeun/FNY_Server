package com.mailservice.fny.analysis.repository;

import com.mailservice.fny.analysis.entity.EmailAnalysis;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAnalysisRepository extends JpaRepository<EmailAnalysis, String> {

    Optional<EmailAnalysis> findByEmailIdAndIsLatestTrue(String emailId);

    long countByEmailMailAccountUserIdAndIsLatestTrueAndNeedsReplyTrue(String userId);

    long countByEmailMailAccountUserIdAndIsLatestTrueAndPriorityLevelIn(String userId, Collection<String> priorityLevels);
}
