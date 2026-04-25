package com.mailservice.fny.mailbox.service;

import com.mailservice.fny.analysis.entity.AnalysisJob;
import com.mailservice.fny.analysis.repository.AnalysisJobRepository;
import com.mailservice.fny.common.IdGenerator;
import com.mailservice.fny.integration.gmail.GmailBody;
import com.mailservice.fny.integration.gmail.GmailClient;
import com.mailservice.fny.integration.gmail.GmailHeader;
import com.mailservice.fny.integration.gmail.GmailListResponse;
import com.mailservice.fny.integration.gmail.GmailMessageRef;
import com.mailservice.fny.integration.gmail.GmailMessageResponse;
import com.mailservice.fny.integration.gmail.GmailPayload;
import com.mailservice.fny.mailbox.dto.MailSyncResponse;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import com.mailservice.fny.mailbox.entity.EmailRecipient;
import com.mailservice.fny.mailbox.entity.MailAccount;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import com.mailservice.fny.mailbox.repository.AppUserRepository;
import com.mailservice.fny.mailbox.repository.EmailMessageRepository;
import com.mailservice.fny.mailbox.repository.EmailRecipientRepository;
import com.mailservice.fny.mailbox.repository.MailAccountRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class GmailSyncService {

    private static final int GMAIL_PAGE_SIZE = 100;
    private static final int MAX_LIMIT = 1000;
    private static final int ANALYSIS_WINDOW_DAYS = 30;
    private static final int CANDIDATE_SCORE_THRESHOLD = 40;
    private static final int INCREMENTAL_LOOKBACK_DAYS = 2;
    private static final String DEV_TOKEN_PREFIX = "{plain-for-dev}";
    private static final List<String> LOW_VALUE_LABELS = List.of(
            "CATEGORY_PROMOTIONS",
            "CATEGORY_SOCIAL",
            "CATEGORY_FORUMS"
    );
    private static final List<String> ACTION_KEYWORDS = List.of(
            "확인",
            "요청",
            "승인",
            "검토",
            "회신",
            "답변",
            "제출",
            "공유",
            "전달",
            "마감",
            "계약",
            "견적",
            "정산",
            "입금",
            "세금계산서",
            "please review",
            "approval",
            "request",
            "reply",
            "deadline",
            "due"
    );
    private static final List<String> MEETING_KEYWORDS = List.of(
            "회의",
            "미팅",
            "일정",
            "참석",
            "초대",
            "meeting",
            "schedule",
            "calendar",
            "invite"
    );
    private static final List<String> INCIDENT_KEYWORDS = List.of(
            "긴급",
            "장애",
            "오류",
            "실패",
            "이슈",
            "urgent",
            "incident",
            "error",
            "failure",
            "critical"
    );
    private static final List<String> AUTO_SENDER_KEYWORDS = List.of(
            "no-reply",
            "noreply",
            "notification",
            "newsletter",
            "marketing",
            "mailer-daemon",
            "bounce",
            "alert"
    );
    private static final List<String> LOW_VALUE_CONTENT_KEYWORDS = List.of(
            "뉴스레터",
            "프로모션",
            "이벤트",
            "광고",
            "할인",
            "인증번호",
            "보안 알림",
            "로그인 알림",
            "newsletter",
            "promotion",
            "sale",
            "verification code",
            "security alert",
            "login alert"
    );

    private final AppUserRepository appUserRepository;
    private final MailAccountRepository mailAccountRepository;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailRecipientRepository emailRecipientRepository;
    private final AnalysisJobRepository analysisJobRepository;
    private final GmailClient gmailClient;
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final TransactionTemplate transactionTemplate;
    private final MailboxDeduplicationService mailboxDeduplicationService;
    private final ConcurrentMap<String, ReentrantLock> syncLocks = new ConcurrentHashMap<>();

    public GmailSyncService(
            AppUserRepository appUserRepository,
            MailAccountRepository mailAccountRepository,
            EmailMessageRepository emailMessageRepository,
            EmailRecipientRepository emailRecipientRepository,
            AnalysisJobRepository analysisJobRepository,
            GmailClient gmailClient,
            ObjectMapper objectMapper,
            IdGenerator idGenerator,
            TransactionTemplate transactionTemplate,
            MailboxDeduplicationService mailboxDeduplicationService
    ) {
        this.appUserRepository = appUserRepository;
        this.mailAccountRepository = mailAccountRepository;
        this.emailMessageRepository = emailMessageRepository;
        this.emailRecipientRepository = emailRecipientRepository;
        this.analysisJobRepository = analysisJobRepository;
        this.gmailClient = gmailClient;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.transactionTemplate = transactionTemplate;
        this.mailboxDeduplicationService = mailboxDeduplicationService;
    }

    public MailSyncResponse sync(String userId, String mailAccountId, int limitParam) {
        ReentrantLock syncLock = syncLocks.computeIfAbsent(mailAccountId, ignored -> new ReentrantLock());
        if (!syncLock.tryLock()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 메일 계정의 동기화가 진행 중입니다.");
        }
        try {
            return syncLocked(userId, mailAccountId, limitParam);
        } finally {
            syncLock.unlock();
        }
    }

    private MailSyncResponse syncLocked(String userId, String mailAccountId, int limitParam) {
        if (!appUserRepository.existsById(userId)) {
            throw new MailboxNotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
        }

        MailAccount account = mailAccountRepository.findByIdAndUser_Id(mailAccountId, userId)
                .orElseThrow(() -> new MailboxNotFoundException("메일 계정을 찾을 수 없습니다. id=" + mailAccountId));

        if (!"GOOGLE".equalsIgnoreCase(account.getProvider())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gmail 계정만 동기화할 수 있습니다.");
        }

        String accessToken = decryptPlaceholder(account.getAccessTokenEncrypted());
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google access token이 없습니다. 다시 로그인해 주세요.");
        }

        boolean syncAll = limitParam <= 0;
        int requestedLimit = syncAll ? Integer.MAX_VALUE : Math.max(1, Math.min(limitParam, MAX_LIMIT));
        String gmailQuery = resolveGmailQuery(account);
        LocalDateTime analysisCutoff = LocalDateTime.now().minusDays(ANALYSIS_WINDOW_DAYS);
        int inserted = 0;
        int skipped = mailboxDeduplicationService.deduplicateUserEmails(userId);
        int fetched = 0;
        int analysisRequested = 0;
        int analysisCompleted = 0;
        int analysisSkipped = 0;
        String pageToken = null;

        do {
            int pageSize = Math.min(GMAIL_PAGE_SIZE, requestedLimit - fetched);
            if (pageSize <= 0) {
                break;
            }

            GmailListResponse listResponse = gmailClient.listMessages(accessToken, pageSize, pageToken, gmailQuery);
            List<GmailMessageRef> refs = listResponse.messages() == null ? List.of() : listResponse.messages();
            fetched += refs.size();

            for (GmailMessageRef ref : refs) {
                if (ref.id() == null || ref.id().isBlank()) {
                    skipped++;
                    continue;
                }
                if (isAlreadySynced(userId, account, ref.id())) {
                    skipped++;
                    continue;
                }

                GmailMessageResponse gmailMessage = gmailClient.getMessage(accessToken, ref.id());
                PersistEmailResult persistResult = persistEmail(userId, account, ref.id(), gmailMessage, analysisCutoff);
                if (!persistResult.inserted()) {
                    skipped++;
                    continue;
                }

                if (persistResult.analysisEligible()) {
                    analysisRequested++;
                } else {
                    analysisSkipped++;
                }
                inserted++;
            }

            pageToken = listResponse.nextPageToken();
        } while (pageToken != null && !pageToken.isBlank() && fetched < requestedLimit);

        LocalDateTime syncedAt = LocalDateTime.now();
        markSynced(account.getId(), syncedAt);
        skipped += mailboxDeduplicationService.deduplicateUserEmails(userId);
        return new MailSyncResponse(
                account.getId(),
                syncAll ? fetched : requestedLimit,
                fetched,
                inserted,
                skipped,
                analysisRequested,
                analysisCompleted,
                analysisSkipped,
                syncedAt
        );
    }

    private String resolveGmailQuery(MailAccount account) {
        if (account.getLastSyncedAt() == null) {
            return null;
        }
        LocalDate afterDate = account.getLastSyncedAt()
                .minusDays(INCREMENTAL_LOOKBACK_DAYS)
                .toLocalDate();
        return "after:" + afterDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    private PersistEmailResult persistEmail(
            String userId,
            MailAccount account,
            String externalMessageId,
            GmailMessageResponse gmailMessage,
            LocalDateTime analysisCutoff
    ) {
        try {
            return transactionTemplate.execute(status -> {
                if (isAlreadySynced(userId, account, externalMessageId)) {
                    return PersistEmailResult.skipped();
                }

                EmailMessage email = toEmailMessage(account, externalMessageId, gmailMessage);
                AnalysisCandidateDecision decision = evaluateAnalysisCandidate(
                        account,
                        email,
                        gmailMessage,
                        analysisCutoff
                );
                email.markAnalysisCandidate(
                        decision.eligible(),
                        decision.score(),
                        String.join(",", decision.reasonCodes()),
                        decision.skippedReason(),
                        LocalDateTime.now()
                );
                emailMessageRepository.save(email);
                saveRecipients(email, gmailMessage);
                if (decision.eligible()) {
                    queueAnalysis(email);
                }
                return PersistEmailResult.inserted(decision.eligible());
            });
        } catch (DataIntegrityViolationException exception) {
            return PersistEmailResult.skipped();
        }
    }

    private boolean isAlreadySynced(String userId, MailAccount account, String externalMessageId) {
        return emailMessageRepository.findByMailAccount_IdAndExternalMessageId(account.getId(), externalMessageId).isPresent()
                || emailMessageRepository.existsByUserProviderAccountEmailAndExternalMessageId(
                userId,
                account.getProvider(),
                account.getAccountEmail(),
                externalMessageId
        );
    }

    private void markSynced(String mailAccountId, LocalDateTime syncedAt) {
        transactionTemplate.executeWithoutResult(status ->
                mailAccountRepository.findById(mailAccountId).ifPresent(account -> account.markSynced(syncedAt))
        );
    }

    private AnalysisCandidateDecision evaluateAnalysisCandidate(
            MailAccount account,
            EmailMessage email,
            GmailMessageResponse message,
            LocalDateTime analysisCutoff
    ) {
        CandidateScore score = calculateCandidateScore(account, email, message);
        if (email.getReceivedAt().isBefore(analysisCutoff)) {
            return new AnalysisCandidateDecision(false, score.value(), score.reasonCodes(), "OLD_MAIL");
        }
        boolean eligible = score.value() >= CANDIDATE_SCORE_THRESHOLD;
        return new AnalysisCandidateDecision(
                eligible,
                score.value(),
                score.reasonCodes(),
                eligible ? null : "LOW_CANDIDATE_SCORE"
        );
    }

    private CandidateScore calculateCandidateScore(MailAccount account, EmailMessage email, GmailMessageResponse message) {
        int score = 0;
        List<String> reasonCodes = new ArrayList<>();
        List<String> labels = message.labelIds() == null ? List.of() : message.labelIds();
        String subject = nullToBlank(email.getSubject());
        String snippet = nullToBlank(email.getMessageSnippet());
        String body = nullToBlank(email.getBodyText());
        String content = (subject + " " + snippet + " " + body).toLowerCase(Locale.ROOT);
        String fromEmail = nullToBlank(email.getFromEmail()).toLowerCase(Locale.ROOT);
        String toHeader = header(message.payload(), "To").toLowerCase(Locale.ROOT);

        if (!email.isRead()) {
            score += 25;
            reasonCodes.add("UNREAD");
        }
        if (email.isStarred()) {
            score += 35;
            reasonCodes.add("STARRED");
        }
        if ("high".equalsIgnoreCase(nullToBlank(email.getImportanceHeader()))) {
            score += 25;
            reasonCodes.add("IMPORTANT_HEADER");
        }
        if (email.isHasAttachment()) {
            score += 15;
            reasonCodes.add("HAS_ATTACHMENT");
        }
        if (!email.getReceivedAt().isBefore(LocalDateTime.now().minusDays(3))) {
            score += 15;
            reasonCodes.add("RECENT_3_DAYS");
        }
        if (toHeader.contains(account.getAccountEmail().toLowerCase(Locale.ROOT))) {
            score += 20;
            reasonCodes.add("DIRECT_TO_ME");
        }
        if (containsAny(content, ACTION_KEYWORDS)) {
            score += 35;
            reasonCodes.add("ACTION_KEYWORD");
        }
        if (containsAny(content, MEETING_KEYWORDS)) {
            score += 20;
            reasonCodes.add("MEETING_KEYWORD");
        }
        if (containsAny(content, INCIDENT_KEYWORDS)) {
            score += 35;
            reasonCodes.add("INCIDENT_KEYWORD");
        }
        if (labels.stream().anyMatch(LOW_VALUE_LABELS::contains)) {
            score -= 45;
            reasonCodes.add("LOW_VALUE_CATEGORY");
        }
        if (containsAny(fromEmail, AUTO_SENDER_KEYWORDS)) {
            score -= 30;
            reasonCodes.add("AUTO_SENDER");
        }
        if (containsAny(content, LOW_VALUE_CONTENT_KEYWORDS)) {
            score -= 35;
            reasonCodes.add("LOW_VALUE_CONTENT");
        }

        return new CandidateScore(score, reasonCodes);
    }

    private AnalysisJob queueAnalysis(EmailMessage email) {
        AnalysisJob job = new AnalysisJob(
                idGenerator.generate("JOB"),
                email,
                "EMAIL_ANALYSIS",
                AnalysisJob.STATUS_PENDING,
                5
        );
        return analysisJobRepository.save(job);
    }

    private EmailMessage toEmailMessage(MailAccount account, String externalMessageId, GmailMessageResponse message) {
        GmailPayload payload = message.payload();
        String from = header(payload, "From");
        Address fromAddress = parseAddress(from);
        BodyParts bodyParts = extractBodyParts(payload);
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
                hasAttachment(payload),
                blankToNull(header(payload, "Importance")),
                rawPayload
        );
    }

    private void saveRecipients(EmailMessage email, GmailMessageResponse message) {
        GmailPayload payload = message.payload();
        List<EmailRecipient> recipients = new ArrayList<>();
        recipients.addAll(parseRecipients(email, "TO", header(payload, "To")));
        recipients.addAll(parseRecipients(email, "CC", header(payload, "Cc")));
        recipients.addAll(parseRecipients(email, "BCC", header(payload, "Bcc")));
        emailRecipientRepository.saveAll(recipients);
    }

    private List<EmailRecipient> parseRecipients(EmailMessage email, String type, String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }

        List<EmailRecipient> recipients = new ArrayList<>();
        for (String token : headerValue.split(",")) {
            Address address = parseAddress(token);
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

    private static String header(GmailPayload payload, String name) {
        if (payload == null || payload.headers() == null) {
            return "";
        }
        return payload.headers().stream()
                .filter(header -> header.name() != null && header.name().equalsIgnoreCase(name))
                .map(GmailHeader::value)
                .findFirst()
                .orElse("");
    }

    private static BodyParts extractBodyParts(GmailPayload payload) {
        StringBuilder text = new StringBuilder();
        StringBuilder html = new StringBuilder();
        collectBody(payload, text, html);
        return new BodyParts(text.toString().strip(), html.toString().strip());
    }

    private static void collectBody(GmailPayload payload, StringBuilder text, StringBuilder html) {
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

    private static boolean hasAttachment(GmailPayload payload) {
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
        return payload.parts().stream().anyMatch(GmailSyncService::hasAttachment);
    }

    private static LocalDateTime resolveReceivedAt(GmailMessageResponse message, GmailPayload payload) {
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

    private static Address parseAddress(String value) {
        if (value == null || value.isBlank()) {
            return new Address("", "");
        }

        String trimmed = value.strip();
        int start = trimmed.lastIndexOf('<');
        int end = trimmed.lastIndexOf('>');
        if (start >= 0 && end > start) {
            String name = trimmed.substring(0, start)
                    .replace("\"", "")
                    .strip();
            String email = trimmed.substring(start + 1, end).strip().toLowerCase(Locale.ROOT);
            return new Address(name, email);
        }

        return new Address("", trimmed.toLowerCase(Locale.ROOT));
    }

    private String serialize(GmailMessageResponse message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JacksonException exception) {
            return "{}";
        }
    }

    private static String decryptPlaceholder(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith(DEV_TOKEN_PREFIX)) {
            return token.substring(DEV_TOKEN_PREFIX.length());
        }
        return token;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private static boolean containsAny(String value, List<String> keywords) {
        String lower = value.toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(lower::contains);
    }

    private record Address(String name, String email) {
    }

    private record BodyParts(String text, String html) {
    }

    private record CandidateScore(int value, List<String> reasonCodes) {
    }

    private record AnalysisCandidateDecision(
            boolean eligible,
            int score,
            List<String> reasonCodes,
            String skippedReason
    ) {
    }

    private record PersistEmailResult(boolean inserted, boolean analysisEligible) {

        static PersistEmailResult inserted(boolean analysisEligible) {
            return new PersistEmailResult(true, analysisEligible);
        }

        static PersistEmailResult skipped() {
            return new PersistEmailResult(false, false);
        }
    }
}
