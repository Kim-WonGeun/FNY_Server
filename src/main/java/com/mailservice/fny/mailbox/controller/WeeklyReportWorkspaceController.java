package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.auth.service.CurrentUserService;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceRequest;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceStatusRequest;
import com.mailservice.fny.mailbox.service.WeeklyReportWorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WeeklyReportWorkspaceController {

    private final WeeklyReportWorkspaceService weeklyReportWorkspaceService;
    private final CurrentUserService currentUserService;

    public WeeklyReportWorkspaceController(
            WeeklyReportWorkspaceService weeklyReportWorkspaceService,
            CurrentUserService currentUserService
    ) {
        this.weeklyReportWorkspaceService = weeklyReportWorkspaceService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me/weekly-reports/{reportId}/workspace")
    public ResponseEntity<WeeklyReportWorkspaceResponse> getMyWeeklyReportWorkspace(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String reportId
    ) {
        String userId = resolveUserId(authentication, request);
        return weeklyReportWorkspaceService
                .getWorkspace(userId, reportId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/me/weekly-reports/{reportId}/workspace")
    public WeeklyReportWorkspaceResponse saveMyWeeklyReportWorkspace(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String reportId,
            @Valid @RequestBody WeeklyReportWorkspaceRequest workspaceRequest
    ) {
        String userId = resolveUserId(authentication, request);
        return weeklyReportWorkspaceService.saveWorkspace(
                userId,
                reportId,
                workspaceRequest
        );
    }

    @PatchMapping("/me/weekly-reports/{reportId}/workspace/status")
    public ResponseEntity<WeeklyReportWorkspaceResponse> updateMyWeeklyReportWorkspaceStatus(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String reportId,
            @Valid @RequestBody WeeklyReportWorkspaceStatusRequest statusRequest
    ) {
        String userId = resolveUserId(authentication, request);
        return weeklyReportWorkspaceService
                .updateWorkspaceStatus(
                        userId,
                        reportId,
                        statusRequest
                )
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private String resolveUserId(Authentication authentication, HttpServletRequest request) {
        return currentUserService.resolveUserId(authentication, request);
    }
}
