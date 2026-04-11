package com.mailservice.fny.analysis.repository;

import com.mailservice.fny.analysis.entity.EmailActionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailActionItemRepository extends JpaRepository<EmailActionItem, String> {

    List<EmailActionItem> findByAnalysisIdOrderByCreatedAtAsc(String analysisId);
}
