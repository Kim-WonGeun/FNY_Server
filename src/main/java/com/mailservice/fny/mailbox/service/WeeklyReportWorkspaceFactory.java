package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.mailbox.entity.AppUser;
import com.mailservice.fny.mailbox.entity.WeeklyMailReport;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportWorkspaceFactory {

    private final IdGenerator idGenerator;

    public WeeklyReportWorkspaceFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public WeeklyReportWorkspace create(
            WeeklyMailReport report,
            AppUser user,
            String draftText,
            String saveStatus,
            String excludedSourceIds
    ) {
        return new WeeklyReportWorkspace(
                idGenerator.generate("WKS"),
                report,
                user,
                draftText,
                saveStatus,
                excludedSourceIds
        );
    }
}
