package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.mailbox.dto.WeeklyReportListItem;
import com.mailservice.fny.mailbox.dto.WeeklyReportResponse;
import com.mailservice.fny.mailbox.service.WeeklyReportService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @PostMapping("/users/{userId}/mail-accounts/{mailAccountId}/weekly-reports")
    public WeeklyReportResponse generateWeeklyReport(
            @PathVariable String userId,
            @PathVariable String mailAccountId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "WEEKLY") String reportType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return weeklyReportService.generate(userId, mailAccountId, days, reportType, startDate, endDate);
    }

    @GetMapping("/users/{userId}/mail-accounts/{mailAccountId}/weekly-reports")
    public List<WeeklyReportListItem> listWeeklyReports(
            @PathVariable String userId,
            @PathVariable String mailAccountId
    ) {
        return weeklyReportService.listReports(userId, mailAccountId);
    }

    @GetMapping("/users/{userId}/weekly-reports/{reportId}")
    public WeeklyReportResponse getWeeklyReport(
            @PathVariable String userId,
            @PathVariable String reportId
    ) {
        return weeklyReportService.getReport(userId, reportId);
    }
}
