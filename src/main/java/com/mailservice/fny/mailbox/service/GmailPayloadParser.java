package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.integration.gmail.GmailBody;
import com.mailservice.fny.integration.gmail.GmailHeader;
import com.mailservice.fny.integration.gmail.GmailPayload;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class GmailPayloadParser {

    public String header(GmailPayload payload, String name) {
        if (payload == null || payload.headers() == null) {
            return "";
        }
        return payload.headers().stream()
                .filter(header -> header.name() != null && header.name().equalsIgnoreCase(name))
                .map(GmailHeader::value)
                .findFirst()
                .orElse("");
    }

    public GmailAddress parseAddress(String value) {
        if (value == null || value.isBlank()) {
            return new GmailAddress("", "");
        }

        String trimmed = value.strip();
        int start = trimmed.lastIndexOf('<');
        int end = trimmed.lastIndexOf('>');
        if (start >= 0 && end > start) {
            String name = trimmed.substring(0, start)
                    .replace("\"", "")
                    .strip();
            String email = trimmed.substring(start + 1, end).strip().toLowerCase(Locale.ROOT);
            return new GmailAddress(name, email);
        }

        return new GmailAddress("", trimmed.toLowerCase(Locale.ROOT));
    }

    public GmailBodyParts extractBodyParts(GmailPayload payload) {
        StringBuilder text = new StringBuilder();
        StringBuilder html = new StringBuilder();
        collectBody(payload, text, html);
        return new GmailBodyParts(text.toString().strip(), html.toString().strip());
    }

    public boolean hasAttachment(GmailPayload payload) {
        if (payload == null) {
            return false;
        }
        if (payload.filename() != null && !payload.filename().isBlank()) {
            return true;
        }
        if (payload.body() != null && payload.body().attachmentId() != null && !payload.body().attachmentId().isBlank()) {
            return true;
        }
        if (payload.parts() == null) {
            return false;
        }
        return payload.parts().stream().anyMatch(this::hasAttachment);
    }

    private void collectBody(GmailPayload payload, StringBuilder text, StringBuilder html) {
        if (payload == null) {
            return;
        }

        GmailBody body = payload.body();
        String decoded = decodeBody(body);
        if (!decoded.isBlank()) {
            if ("text/html".equalsIgnoreCase(payload.mimeType())) {
                appendPart(html, decoded);
            } else if ("text/plain".equalsIgnoreCase(payload.mimeType())) {
                appendPart(text, decoded);
            }
        }

        if (payload.parts() != null) {
            for (GmailPayload part : payload.parts()) {
                collectBody(part, text, html);
            }
        }
    }

    private static String decodeBody(GmailBody body) {
        if (body == null || body.data() == null || body.data().isBlank()) {
            return "";
        }
        byte[] decoded = Base64.getUrlDecoder().decode(body.data());
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private static void appendPart(StringBuilder builder, String part) {
        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }
        builder.append(part.strip());
    }
}

record GmailAddress(String name, String email) {
}

record GmailBodyParts(String text, String html) {
}
