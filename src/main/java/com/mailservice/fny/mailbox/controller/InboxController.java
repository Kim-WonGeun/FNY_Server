package com.mailservice.fny.mailbox.controller;

import com.mailservice.fny.auth.service.CurrentUserService;
import com.mailservice.fny.mailbox.dto.EmailListResponse;
import com.mailservice.fny.mailbox.dto.MailboxOverviewResponse;
import com.mailservice.fny.mailbox.service.InboxService;
import com.mailservice.fny.mailbox.service.MailboxOverviewService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InboxController {

    private final InboxService inboxService;
    private final MailboxOverviewService mailboxOverviewService;
    private final CurrentUserService currentUserService;

    public InboxController(
            InboxService inboxService,
            MailboxOverviewService mailboxOverviewService,
            CurrentUserService currentUserService
    ) {
        this.inboxService = inboxService;
        this.mailboxOverviewService = mailboxOverviewService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/users/{userId}/emails")
    public List<EmailListResponse> getInbox(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "false") boolean highPriorityOnly,
            @RequestParam(defaultValue = "false") boolean needsReplyOnly,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean searchBody
    ) {
        return inboxService.getInbox(userId, unreadOnly, highPriorityOnly, needsReplyOnly, query, sender, startDate, endDate, searchBody);
    }

    @GetMapping("/users/{userId}/overview")
    public MailboxOverviewResponse getOverview(@PathVariable String userId) {
        return mailboxOverviewService.getOverview(userId);
    }

    @GetMapping("/me/emails")
    public List<EmailListResponse> getMyEmails(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "false") boolean highPriorityOnly,
            @RequestParam(defaultValue = "false") boolean needsReplyOnly,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean searchBody
    ) {
        return inboxService.getInbox(
                currentUserService.resolveUserId(authentication, request),
                unreadOnly,
                highPriorityOnly,
                needsReplyOnly,
                query,
                sender,
                startDate,
                endDate,
                searchBody
        );
    }

    @GetMapping("/me/overview")
    public MailboxOverviewResponse getMyOverview(Authentication authentication, HttpServletRequest request) {
        return mailboxOverviewService.getOverview(currentUserService.resolveUserId(authentication, request));
    }
}
