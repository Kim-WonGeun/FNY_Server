package com.mailservice.fny.mailbox.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class WeeklyReportPeriodResolver {

    WeeklyReportPeriodRange resolve(int daysParam, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            int days = Math.max(1, Math.min(daysParam, 30));
            LocalDateTime periodEnd = LocalDateTime.now();
            return new WeeklyReportPeriodRange(periodEnd.minusDays(days), periodEnd, "최근 " + days + "일간");
        }

        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일과 종료일을 모두 입력해야 합니다.");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 늦을 수 없습니다.");
        }
        if (startDate.plusDays(30).isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "조회 기간은 최대 31일까지 가능합니다.");
        }

        return new WeeklyReportPeriodRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                startDate + " ~ " + endDate
        );
    }
}

record WeeklyReportPeriodRange(
        LocalDateTime start,
        LocalDateTime endExclusive,
        String label
) {
}
