package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.mailbox.dto.InboxEmailSummary;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MailboxAttentionPolicy {

    private static final Set<String> HIGH_PRIORITY_LEVELS = Set.of("P1", "P2");

    public boolean isHighPriority(String priorityLevel) {
        return priorityLevel != null && HIGH_PRIORITY_LEVELS.contains(priorityLevel);
    }

    public boolean isOpenAttention(InboxEmailSummary email) {
        return !EmailMessage.isClosedAttentionStatus(email.attentionStatus());
    }

    public boolean isSpotlightCandidate(InboxEmailSummary email) {
        return isOpenAttention(email)
                && (isHighPriority(email.priorityLevel())
                || Boolean.TRUE.equals(email.needsReply())
                || !email.isRead()
                || email.isStarred());
    }
}
