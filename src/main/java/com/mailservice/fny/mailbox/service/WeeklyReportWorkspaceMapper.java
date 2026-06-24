package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.WeeklyReportWorkspaceResponse;
import com.mailservice.fny.mailbox.entity.WeeklyReportWorkspace;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportWorkspaceMapper {

    private final WeeklyReportWorkspaceSourceIdsCodec sourceIdsCodec;

    public WeeklyReportWorkspaceMapper(WeeklyReportWorkspaceSourceIdsCodec sourceIdsCodec) {
        this.sourceIdsCodec = sourceIdsCodec;
    }

    WeeklyReportWorkspaceResponse toResponse(WeeklyReportWorkspace workspace) {
        return toResponse(workspace, sourceIdsCodec.deserialize(workspace.getExcludedSourceIds()));
    }

    WeeklyReportWorkspaceResponse toResponse(
            WeeklyReportWorkspace workspace,
            List<String> excludedSourceIds
    ) {
        return WeeklyReportWorkspaceResponse.from(workspace, excludedSourceIds);
    }
}
