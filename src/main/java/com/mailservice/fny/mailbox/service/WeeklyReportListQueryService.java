package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspaceStatus;
import com.mailservice.fny.mailbox.repository.WeeklyMailReportRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WeeklyReportListQueryService {

    private final WeeklyMailReportRepository weeklyMailReportRepository;
    private final WeeklyReportWorkspaceService weeklyReportWorkspaceService;

    public WeeklyReportListQueryService(
            WeeklyMailReportRepository weeklyMailReportRepository,
            WeeklyReportWorkspaceService weeklyReportWorkspaceService
    ) {
        this.weeklyMailReportRepository = weeklyMailReportRepository;
        this.weeklyReportWorkspaceService = weeklyReportWorkspaceService;
    }

    public List<WeeklyReportListItem> findListItems(String userId, String mailAccountId) {
        List<WeeklyMailReport> reports = weeklyMailReportRepository.findTop20ByMailAccountIdOrderByCreatedAtDesc(mailAccountId);
        Map<String, String> workspaceStatuses = weeklyReportWorkspaceService.findActiveStatuses(userId, reports);

        return reports.stream()
                .map(report -> WeeklyReportListItem.from(
                        report,
                        workspaceStatuses.getOrDefault(report.getId(), WeeklyReportWorkspaceStatus.NONE)
                ))
                .toList();
    }
}
