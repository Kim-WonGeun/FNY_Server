package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportWorkspaceRepository extends JpaRepository<WeeklyReportWorkspace, String> {

    Optional<WeeklyReportWorkspace> findByReportIdAndUserId(String reportId, String userId);

    List<WeeklyReportWorkspace> findByReportIdInAndUserId(List<String> reportIds, String userId);
}
