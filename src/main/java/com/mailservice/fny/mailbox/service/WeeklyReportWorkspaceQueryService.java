package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspaceStatus;
import com.mailservice.fny.mailbox.repository.WeeklyReportWorkspaceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyReportWorkspaceQueryService {

    private final MailboxResourceResolver mailboxResourceResolver;
    private final WeeklyReportWorkspaceRepository weeklyReportWorkspaceRepository;
    private final WeeklyReportWorkspaceMapper weeklyReportWorkspaceMapper;

    public WeeklyReportWorkspaceQueryService(
            MailboxResourceResolver mailboxResourceResolver,
            WeeklyReportWorkspaceRepository weeklyReportWorkspaceRepository,
            WeeklyReportWorkspaceMapper weeklyReportWorkspaceMapper
    ) {
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.weeklyReportWorkspaceRepository = weeklyReportWorkspaceRepository;
        this.weeklyReportWorkspaceMapper = weeklyReportWorkspaceMapper;
    }

    public Optional<WeeklyReportWorkspaceResponse> getWorkspace(String userId, String reportId) {
        mailboxResourceResolver.getRequiredWeeklyReport(userId, reportId);
        return weeklyReportWorkspaceRepository.findByReportIdAndUserId(reportId, userId)
                .filter(workspace -> WeeklyReportWorkspaceStatus.isActive(workspace.getSaveStatus()))
                .map(weeklyReportWorkspaceMapper::toResponse);
    }

    public Map<String, String> findActiveStatuses(String userId, List<WeeklyMailReport> reports) {
        if (reports.isEmpty()) {
            return Map.of();
        }

        return weeklyReportWorkspaceRepository
                .findByReportIdInAndUserId(reports.stream().map(WeeklyMailReport::getId).toList(), userId)
                .stream()
                .filter(workspace -> WeeklyReportWorkspaceStatus.isActive(workspace.getSaveStatus()))
                .collect(
                        HashMap::new,
                        (statuses, workspace) -> statuses.put(workspace.getReport().getId(), workspace.getSaveStatus()),
                        HashMap::putAll
                );
    }
}
