package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.InboxEmailSummary;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class InboxEmailFilter {

    private final MailboxAttentionPolicy mailboxAttentionPolicy;

    public InboxEmailFilter(MailboxAttentionPolicy mailboxAttentionPolicy) {
        this.mailboxAttentionPolicy = mailboxAttentionPolicy;
    }

    public boolean matches(
            InboxEmailSummary email,
            boolean unreadOnly,
            boolean highPriorityOnly,
            boolean needsReplyOnly,
            String query,
            String sender,
            LocalDate startDate,
            LocalDate endDate,
            boolean searchBody
    ) {
        return (!unreadOnly || !email.isRead())
                && (!highPriorityOnly || (mailboxAttentionPolicy.isOpenAttention(email) && mailboxAttentionPolicy.isHighPriority(email.priorityLevel())))
                && (!needsReplyOnly || (mailboxAttentionPolicy.isOpenAttention(email) && Boolean.TRUE.equals(email.needsReply())))
                && matchesQuery(email, query, searchBody)
                && matchesSender(email, sender)
                && matchesReceivedRange(email, startDate, endDate);
    }

    private boolean matchesQuery(InboxEmailSummary email, String query, boolean searchBody) {
        String normalized = normalizeSearchText(query);
        if (normalized.isBlank()) {
            return true;
        }

        return containsIgnoreCase(email.subject(), normalized)
                || containsIgnoreCase(email.messageSnippet(), normalized)
                || containsIgnoreCase(email.shortSummary(), normalized)
                || containsIgnoreCase(email.fromName(), normalized)
                || containsIgnoreCase(email.fromEmail(), normalized)
                || (searchBody && (
                containsIgnoreCase(email.bodyText(), normalized)
                        || containsIgnoreCase(email.bodyHtml(), normalized)
        ));
    }

    private boolean matchesSender(InboxEmailSummary email, String sender) {
        String normalized = normalizeSearchText(sender);
        if (normalized.isBlank()) {
            return true;
        }

        return containsIgnoreCase(email.fromName(), normalized)
                || containsIgnoreCase(email.fromEmail(), normalized);
    }

    private boolean matchesReceivedRange(InboxEmailSummary email, LocalDate startDate, LocalDate endDate) {
        LocalDate receivedDate = email.receivedAt().toLocalDate();
        return (startDate == null || !receivedDate.isBefore(startDate))
                && (endDate == null || !receivedDate.isAfter(endDate));
    }

    private String normalizeSearchText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
