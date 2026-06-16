package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.auth.service.CurrentUserService;
import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
import com.mailservice.fny.mailbox.entity.WeeklyMailReportType;
import com.mailservice.fny.mailbox.service.WeeklyReportService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MyWeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final CurrentUserService currentUserService;

    public MyWeeklyReportController(
            WeeklyReportService weeklyReportService,
            CurrentUserService currentUserService
    ) {
        this.weeklyReportService = weeklyReportService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/me/mail-accounts/{mailAccountId}/weekly-reports")
    public WeeklyReportResponse generateMyWeeklyReport(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String mailAccountId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = WeeklyMailReportType.WEEKLY) String reportType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        String userId = resolveUserId(authentication, request);
        return weeklyReportService.generate(
                userId,
                mailAccountId,
                days,
                reportType,
                startDate,
                endDate
        );
    }

    @GetMapping("/me/mail-accounts/{mailAccountId}/weekly-reports")
    public List<WeeklyReportListItem> listMyWeeklyReports(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String mailAccountId
    ) {
        String userId = resolveUserId(authentication, request);
        return weeklyReportService.listReports(
                userId,
                mailAccountId
        );
    }

    @GetMapping("/me/weekly-reports/{reportId}")
    public WeeklyReportResponse getMyWeeklyReport(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String reportId
    ) {
        return weeklyReportService.getReport(resolveUserId(authentication, request), reportId);
    }

    private String resolveUserId(Authentication authentication, HttpServletRequest request) {
        return currentUserService.resolveUserId(authentication, request);
    }
}
