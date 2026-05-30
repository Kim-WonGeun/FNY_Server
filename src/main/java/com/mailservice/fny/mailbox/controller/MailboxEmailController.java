package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.analysis.dto.EmailAnalysisResponse;
import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.service.MailboxAnalysisJobService;
import com.mailservice.fny.mailbox.service.MailboxAttentionService;
import com.mailservice.fny.mailbox.service.MailboxDetailService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
public class MailboxEmailController {

    private final MailboxDetailService mailboxDetailService;
    private final MailboxAttentionService mailboxAttentionService;
    private final MailboxAnalysisJobService mailboxAnalysisJobService;

    public MailboxEmailController(
            MailboxDetailService mailboxDetailService,
            MailboxAttentionService mailboxAttentionService,
            MailboxAnalysisJobService mailboxAnalysisJobService
    ) {
        this.mailboxDetailService = mailboxDetailService;
        this.mailboxAttentionService = mailboxAttentionService;
        this.mailboxAnalysisJobService = mailboxAnalysisJobService;
    }

    @GetMapping("/{emailId}")
    public EmailDetailResponse getEmailDetail(@PathVariable String emailId) {
        return mailboxDetailService.getEmailDetail(emailId);
    }

    @GetMapping("/{emailId}/analyses")
    public List<EmailAnalysisResponse> getEmailAnalyses(@PathVariable String emailId) {
        return mailboxDetailService.getEmailAnalyses(emailId);
    }

    @PatchMapping("/{emailId}/attention-resolved")
    public EmailDetailResponse updateAttentionResolved(
            @PathVariable String emailId,
            @RequestParam(defaultValue = "true") boolean resolved
    ) {
        return mailboxAttentionService.updateAttentionResolved(emailId, resolved);
    }

    @PatchMapping("/{emailId}/attention-status")
    public EmailDetailResponse updateAttentionStatus(
            @PathVariable String emailId,
            @RequestParam(defaultValue = "NEEDS_ATTENTION") String status
    ) {
        return mailboxAttentionService.updateAttentionStatus(emailId, status);
    }

    @PostMapping("/{emailId}/analysis-jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisJobCreateResponse queueAnalysisJob(@PathVariable String emailId) {
        return mailboxAnalysisJobService.queueAnalysisJob(emailId);
    }
}
