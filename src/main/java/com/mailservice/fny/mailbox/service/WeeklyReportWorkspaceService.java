package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceRequest;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceStatusRequest;
import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspaceStatus;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.WeeklyReportWorkspaceRepository;
import java.util.List;
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
    private final WeeklyReportWorkspaceFactory weeklyReportWorkspaceFactory;
    private final WeeklyReportWorkspaceSourceIdsCodec sourceIdsCodec;

    public WeeklyReportWorkspaceService(
            MailboxUserValidator mailboxUserValidator,
            MailboxResourceResolver mailboxResourceResolver,
            WeeklyReportWorkspaceRepository weeklyReportWorkspaceRepository,
            WeeklyReportWorkspaceMapper weeklyReportWorkspaceMapper,
            WeeklyReportWorkspaceFactory weeklyReportWorkspaceFactory,
            WeeklyReportWorkspaceSourceIdsCodec sourceIdsCodec
    ) {
        this.mailboxUserValidator = mailboxUserValidator;
        this.mailboxResourceResolver = mailboxResourceResolver;
        this.weeklyReportWorkspaceRepository = weeklyReportWorkspaceRepository;
        this.weeklyReportWorkspaceMapper = weeklyReportWorkspaceMapper;
        this.weeklyReportWorkspaceFactory = weeklyReportWorkspaceFactory;
        this.sourceIdsCodec = sourceIdsCodec;
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
        List<String> excludedSourceIds = sourceIdsCodec.normalize(request.excludedSourceIds());
        String excludedJson = sourceIdsCodec.serialize(excludedSourceIds);

        WeeklyReportWorkspace workspace = weeklyReportWorkspaceRepository.findByReportIdAndUserId(reportId, userId)
                .map(existing -> {
                    existing.update(request.draftText(), saveStatus, excludedJson);
                    return existing;
                })
                .orElseGet(() -> weeklyReportWorkspaceFactory.create(
                        report,
                        user,
                        request.draftText(),
                        saveStatus,
                        excludedJson
        ));
        WeeklyReportWorkspace saved = weeklyReportWorkspaceRepository.save(workspace);
        return weeklyReportWorkspaceMapper.toResponse(saved, excludedSourceIds);
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
        if (WeeklyReportWorkspaceStatus.isArchived(saveStatus)) {
            return Optional.empty();
        }
        return Optional.of(weeklyReportWorkspaceMapper.toResponse(saved));
    }

    private WeeklyMailReport ensureReportAccess(String userId, String reportId) {
        return mailboxResourceResolver.getRequiredWeeklyReport(userId, reportId);
    }
}
