package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceRequest;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceStatusRequest;
import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspaceStatus;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.WeeklyReportWorkspaceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WeeklyReportWorkspaceService {

    private final MailboxUserValidator mailboxUserValidator;
    private final MailboxResourceResolver mailboxResourceResolver;
    private final WeeklyReportWorkspaceRepository weeklyReportWorkspaceRepository;
    private final WeeklyReportWorkspaceMapper weeklyReportWorkspaceMapper;
    private final IdGenerator idGenerator;

    public WeeklyReportWorkspaceService(
            MailboxUserValidator mailboxUserValidator,
            MailboxResourceResolver mailboxResourceResolver,
            WeeklyReportWorkspaceRepository weeklyReportWorkspaceRepository,
            WeeklyReportWorkspaceMapper weeklyReportWorkspaceMapper,
            IdGenerator idGenerator
    ) {
        this.mailboxUserValidator = mailboxUserValidator;
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.weeklyReportWorkspaceRepository = weeklyReportWorkspaceRepository;
        this.weeklyReportWorkspaceMapper = weeklyReportWorkspaceMapper;
        this.idGenerator = idGenerator;
    }

    public Map<String, String> findActiveStatuses(String userId, List<WeeklyMailReport> reports) {
        if (reports.isEmpty()) {
            return Map.of();
        }

        return weeklyReportWorkspaceRepository
                .findByReportIdInAndUserId(reports.stream().map(WeeklyMailReport::getId).toList(), userId)
                .stream()
                .filter(workspace -> !WeeklyReportWorkspaceStatus.ARCHIVED.equals(workspace.getSaveStatus()))
                .collect(HashMap::new, (map, workspace) -> map.put(workspace.getReport().getId(), workspace.getSaveStatus()), HashMap::putAll);
    }

    public Optional<WeeklyReportWorkspaceResponse> getWorkspace(String userId, String reportId) {
        ensureReportAccess(userId, reportId);
        return weeklyReportWorkspaceRepository.findByReportIdAndUserId(reportId, userId)
                .filter(workspace -> !WeeklyReportWorkspaceStatus.ARCHIVED.equals(workspace.getSaveStatus()))
                .map(weeklyReportWorkspaceMapper::toResponse);
    }

    @Transactional
    public WeeklyReportWorkspaceResponse saveWorkspace(
            String userId,
            String reportId,
            WeeklyReportWorkspaceRequest request
    ) {
        AppUser user = mailboxUserValidator.getRequiredUser(userId);
        WeeklyMailReport report = ensureReportAccess(userId, reportId);
        String saveStatus = WeeklyReportWorkspaceStatus.normalizeSaveStatus(request.saveStatus());
        List<String> excludedSourceIds = weeklyReportWorkspaceMapper.normalizeExcludedSourceIds(request.excludedSourceIds());
        String excludedJson = weeklyReportWorkspaceMapper.serializeExcludedSourceIds(excludedSourceIds);

        WeeklyReportWorkspace workspace = weeklyReportWorkspaceRepository.findByReportIdAndUserId(reportId, userId)
                .orElseGet(() -> new WeeklyReportWorkspace(
                        idGenerator.generate("WKS"),
                        report,
                        user,
                        request.draftText(),
                        saveStatus,
                        excludedJson
                ));
        workspace.update(request.draftText(), saveStatus, excludedJson);
        WeeklyReportWorkspace saved = weeklyReportWorkspaceRepository.save(workspace);
        return WeeklyReportWorkspaceResponse.from(saved, excludedSourceIds);
    }

    @Transactional
    public Optional<WeeklyReportWorkspaceResponse> updateWorkspaceStatus(
            String userId,
            String reportId,
            WeeklyReportWorkspaceStatusRequest request
    ) {
        ensureReportAccess(userId, reportId);
        WeeklyReportWorkspace workspace = weeklyReportWorkspaceRepository.findByReportIdAndUserId(reportId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("보고서 저장본을 찾을 수 없습니다. reportId=" + reportId));
        String saveStatus = WeeklyReportWorkspaceStatus.normalizeStatusUpdate(request.saveStatus());
        workspace.update(workspace.getDraftText(), saveStatus, workspace.getExcludedSourceIds());
        WeeklyReportWorkspace saved = weeklyReportWorkspaceRepository.save(workspace);
        if (WeeklyReportWorkspaceStatus.ARCHIVED.equals(saveStatus)) {
            return Optional.empty();
        }
        return Optional.of(weeklyReportWorkspaceMapper.toResponse(saved));
    }

    private WeeklyMailReport ensureReportAccess(String userId, String reportId) {
        return mailboxResourceResolver.getRequiredWeeklyReport(userId, reportId);
    }
}
