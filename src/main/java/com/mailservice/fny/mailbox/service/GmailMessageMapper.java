package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.integration.gmail.GmailMessageResponse;
import com.mailservice.fny.integration.gmail.GmailPayload;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.EmailRecipient;
import com.mailservice.fny.mailbox.entity.MailAccount;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class GmailMessageMapper {

    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final GmailPayloadParser gmailPayloadParser;

    public GmailMessageMapper(
            ObjectMapper objectMapper,
            IdGenerator idGenerator,
            GmailPayloadParser gmailPayloadParser
    ) {
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.gmailPayloadParser = gmailPayloadParser;
    }

    public EmailMessage toEmailMessage(
            MailAccount account,
            String externalMessageId,
            GmailMessageResponse message
    ) {
        GmailPayload payload = message.payload();
        String from = header(payload, "From");
        GmailAddress fromAddress = gmailPayloadParser.parseAddress(from);
        GmailBodyParts bodyParts = gmailPayloadParser.extractBodyParts(payload);
        LocalDateTime receivedAt = resolveReceivedAt(message, payload);
        String rawPayload = serialize(message);
        List<String> labels = message.labelIds() == null ? List.of() : message.labelIds();

        return new EmailMessage(
                idGenerator.generate("EML"),
                account,
                externalMessageId,
                message.threadId(),
                header(payload, "Message-ID"),
                blankToNull(header(payload, "Subject")),
                blankToNull(bodyParts.text()),
                blankToNull(bodyParts.html()),
                blankToNull(message.snippet()),
                fromAddress.name(),
                fromAddress.email().isBlank() ? account.getAccountEmail() : fromAddress.email(),
                receivedAt,
                receivedAt,
                !labels.contains("UNREAD"),
                labels.contains("STARRED"),
                gmailPayloadParser.hasAttachment(payload),
                blankToNull(header(payload, "Importance")),
                rawPayload
        );
    }

    public List<EmailRecipient> toRecipients(EmailMessage email, GmailMessageResponse message) {
        GmailPayload payload = message.payload();
        List<EmailRecipient> recipients = new ArrayList<>();
        recipients.addAll(parseRecipients(email, "TO", header(payload, "To")));
        recipients.addAll(parseRecipients(email, "CC", header(payload, "Cc")));
        recipients.addAll(parseRecipients(email, "BCC", header(payload, "Bcc")));
        return recipients;
    }

    public String header(GmailPayload payload, String name) {
        return gmailPayloadParser.header(payload, name);
    }

    private List<EmailRecipient> parseRecipients(EmailMessage email, String type, String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }

        List<EmailRecipient> recipients = new ArrayList<>();
        for (String token : headerValue.split(",")) {
            GmailAddress address = gmailPayloadParser.parseAddress(token);
            if (!address.email().isBlank()) {
                recipients.add(new EmailRecipient(
                        idGenerator.generate("ERC"),
                        email,
                        type,
                        blankToNull(address.name()),
                        address.email()
                ));
            }
        }
        return recipients;
    }

    private LocalDateTime resolveReceivedAt(GmailMessageResponse message, GmailPayload payload) {
        if (message.internalDate() != null && !message.internalDate().isBlank()) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(message.internalDate())),
                    ZoneId.systemDefault()
            );
        }

        String dateHeader = header(payload, "Date");
        if (!dateHeader.isBlank()) {
            try {
                return LocalDateTime.ofInstant(
                        DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateHeader, Instant::from),
                        ZoneId.systemDefault()
                );
            } catch (DateTimeParseException ignored) {
                return LocalDateTime.now();
            }
        }

        return LocalDateTime.now();
    }

    private String serialize(GmailMessageResponse message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JacksonException exception) {
            return "{}";
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
