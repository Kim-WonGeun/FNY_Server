package com.mailservice.fny.analysis.dto;

import com.mailservice.fny.analysis.entity.EmailActionItem;
import java.time.LocalDateTime;

public record EmailActionItemResponse(
        String id,
        String actionText,
        String actionType,
        String priorityLevel,
        LocalDateTime dueAt,
        boolean completed
) {

    public static EmailActionItemResponse from(EmailActionItem item) {
        return new EmailActionItemResponse(
                item.getId(),
                item.getActionText(),
                item.getActionType(),
                item.getPriorityLevel(),
                item.getDueAt(),
                item.isCompleted()
        );
    }
}
