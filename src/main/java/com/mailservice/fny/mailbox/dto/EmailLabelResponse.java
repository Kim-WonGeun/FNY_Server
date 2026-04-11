package com.mailservice.fny.mailbox.dto;

import com.mailservice.fny.mailbox.entity.EmailLabel;
import java.math.BigDecimal;

public record EmailLabelResponse(
        String code,
        String name,
        String type,
        String color,
        BigDecimal confidenceScore
) {

    public static EmailLabelResponse from(EmailLabel emailLabel) {
        return new EmailLabelResponse(
                emailLabel.getLabel().getLabelCode(),
                emailLabel.getLabel().getLabelName(),
                emailLabel.getLabel().getLabelType(),
                emailLabel.getLabel().getColor(),
                emailLabel.getConfidenceScore()
        );
    }
}
