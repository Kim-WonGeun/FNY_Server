package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.auth.service.CurrentUserService;
import com.mailservice.fny.mailbox.dto.MailAccountResponse;
import com.mailservice.fny.mailbox.dto.MailSyncResponse;
import com.mailservice.fny.mailbox.service.GmailSyncService;
import com.mailservice.fny.mailbox.service.MailAccountQueryService;
import jakarta.servlet.http.HttpServletRequest;
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
public class MailAccountController {

    private final MailAccountQueryService mailAccountQueryService;
    private final GmailSyncService gmailSyncService;
    private final CurrentUserService currentUserService;

    public MailAccountController(
            MailAccountQueryService mailAccountQueryService,
            GmailSyncService gmailSyncService,
            CurrentUserService currentUserService
    ) {
        this.mailAccountQueryService = mailAccountQueryService;
        this.gmailSyncService = gmailSyncService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/users/{userId}/mail-accounts")
    public List<MailAccountResponse> getMailAccounts(@PathVariable String userId) {
        return mailAccountQueryService.getMailAccounts(userId);
    }

    @PostMapping("/users/{userId}/mail-accounts/{mailAccountId}/sync")
    public MailSyncResponse syncMailAccount(
            @PathVariable String userId,
            @PathVariable String mailAccountId,
            @RequestParam(defaultValue = "0") int limit
    ) {
        return gmailSyncService.sync(userId, mailAccountId, limit);
    }

    @GetMapping("/me/mail-accounts")
    public List<MailAccountResponse> getMyMailAccounts(Authentication authentication, HttpServletRequest request) {
        return mailAccountQueryService.getMailAccounts(resolveUserId(authentication, request));
    }

    @PostMapping("/me/mail-accounts/{mailAccountId}/sync")
    public MailSyncResponse syncMyMailAccount(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String mailAccountId,
            @RequestParam(defaultValue = "0") int limit
    ) {
        return gmailSyncService.sync(resolveUserId(authentication, request), mailAccountId, limit);
    }

    private String resolveUserId(Authentication authentication, HttpServletRequest request) {
        return currentUserService.resolveUserId(authentication, request);
    }
}
