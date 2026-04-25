package com.mailservice.fny.analysis.repository;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, String> {

    List<AnalysisJob> findByEmailIdOrderByCreatedAtDesc(String emailId);

    long countByEmailMailAccountUserIdAndStatus(String userId, String status);

    long countByEmailMailAccountUserIdAndStatusIn(String userId, Collection<String> statuses);
}
