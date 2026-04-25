package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.analysis.dto.AnalysisJobCreateResponse;
import com.mailservice.fny.mailbox.dto.EmailDetailResponse;
import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.dto.MailAccountResponse;
import com.mailservice.fny.mailbox.dto.MailSyncResponse;
import com.mailservice.fny.mailbox.dto.MailboxOverviewResponse;
import com.mailservice.fny.mailbox.service.GmailSyncService;
import com.mailservice.fny.mailbox.service.MailboxService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MailboxController {

    private final MailboxService mailboxService;
    private final GmailSyncService gmailSyncService;

    public MailboxController(MailboxService mailboxService, GmailSyncService gmailSyncService) {
        this.mailboxService = mailboxService;
        this.gmailSyncService = gmailSyncService;
    }

    @GetMapping("/users/{userId}/mail-accounts")
    public List<MailAccountResponse> getMailAccounts(@PathVariable String userId) {
        return mailboxService.getMailAccounts(userId);
    }

    @GetMapping("/users/{userId}/emails")
    public List<EmailListResponse> getInbox(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "false") boolean highPriorityOnly,
            @RequestParam(defaultValue = "false") boolean needsReplyOnly
    ) {
        return mailboxService.getInbox(userId, unreadOnly, highPriorityOnly, needsReplyOnly);
    }

    @GetMapping("/users/{userId}/overview")
    public MailboxOverviewResponse getOverview(@PathVariable String userId) {
        return mailboxService.getOverview(userId);
    }

    @PostMapping("/users/{userId}/mail-accounts/{mailAccountId}/sync")
    public MailSyncResponse syncMailAccount(
            @PathVariable String userId,
            @PathVariable String mailAccountId,
            @RequestParam(defaultValue = "0") int limit
    ) {
        return gmailSyncService.sync(userId, mailAccountId, limit);
    }

    @GetMapping("/emails/{emailId}")
    public EmailDetailResponse getEmailDetail(@PathVariable String emailId) {
        return mailboxService.getEmailDetail(emailId);
    }

    @PatchMapping("/emails/{emailId}/attention-resolved")
    public EmailDetailResponse updateAttentionResolved(
            @PathVariable String emailId,
            @RequestParam(defaultValue = "true") boolean resolved
    ) {
        return mailboxService.updateAttentionResolved(emailId, resolved);
    }

    @PatchMapping("/emails/{emailId}/attention-status")
    public EmailDetailResponse updateAttentionStatus(
            @PathVariable String emailId,
            @RequestParam(defaultValue = "NEEDS_ATTENTION") String status
    ) {
        return mailboxService.updateAttentionStatus(emailId, status);
    }

    @PostMapping("/emails/{emailId}/analysis-jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AnalysisJobCreateResponse queueAnalysisJob(@PathVariable String emailId) {
        return mailboxService.queueAnalysisJob(emailId);
    }
}
