package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.analysis.entity.EmailAnalysis;
import com.mailservice.fny.mailbox.dto.InboxEmailSummary;
import com.mailservice.fny.mailbox.entity.EmailMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, String> {

    @Query("""
            select new com.mailservice.fny.mailbox.dto.InboxEmailSummary(
                email.id,
                email.subject,
                email.messageSnippet,
                email.fromName,
                email.fromEmail,
                email.receivedAt,
                email.isRead,
                email.isStarred,
                email.hasAttachment,
                analysis.category,
                analysis.priorityLevel,
                analysis.importanceScore,
                analysis.urgencyScore,
                analysis.shortSummary,
                analysis.needsReply,
                email.analysisEligible,
                email.analysisCandidateScore,
                email.analysisCandidateReasons,
                email.analysisSkippedReason,
                email.analysisCandidateEvaluatedAt,
                email.attentionResolved,
                email.attentionResolvedAt,
                email.attentionStatus,
                email.attentionStatusUpdatedAt
            )
            from EmailMessage email
            join email.mailAccount account
            left join EmailAnalysis analysis on analysis.email = email and analysis.isLatest = true
            where account.user.id = :userId
            order by
                case
                    when email.attentionStatus <> 'NEEDS_ATTENTION' then 5
                    when analysis.priorityLevel = 'P1' then 1
                    when analysis.priorityLevel = 'P2' then 2
                    when analysis.priorityLevel = 'P3' then 3
                    else 4
                end,
                coalesce(analysis.importanceScore, 0) desc,
                email.receivedAt desc
            """)
    List<InboxEmailSummary> findInboxByUserId(@Param("userId") String userId);

    @Query("""
            select new com.mailservice.fny.mailbox.dto.InboxEmailSummary(
                email.id,
                email.subject,
                email.messageSnippet,
                email.fromName,
                email.fromEmail,
                email.receivedAt,
                email.isRead,
                email.isStarred,
                email.hasAttachment,
                analysis.category,
                analysis.priorityLevel,
                analysis.importanceScore,
                analysis.urgencyScore,
                analysis.shortSummary,
                analysis.needsReply,
                email.analysisEligible,
                email.analysisCandidateScore,
                email.analysisCandidateReasons,
                email.analysisSkippedReason,
                email.analysisCandidateEvaluatedAt,
                email.attentionResolved,
                email.attentionResolvedAt,
                email.attentionStatus,
                email.attentionStatusUpdatedAt
            )
            from EmailMessage email
            join email.mailAccount account
            left join EmailAnalysis analysis on analysis.email = email and analysis.isLatest = true
            where account.user.id = :userId
            order by email.receivedAt desc
            """)
    List<InboxEmailSummary> findMailboxByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"mailAccount", "mailAccount.user"})
    Optional<EmailMessage> findWithMailAccountById(String id);

    Optional<EmailMessage> findByMailAccount_IdAndExternalMessageId(String mailAccountId, String externalMessageId);

    @Query("""
            select count(email) > 0
            from EmailMessage email
            join email.mailAccount account
            where account.user.id = :userId
              and upper(account.provider) = upper(:provider)
              and lower(account.accountEmail) = lower(:accountEmail)
              and email.externalMessageId = :externalMessageId
            """)
    boolean existsByUserProviderAccountEmailAndExternalMessageId(
            @Param("userId") String userId,
            @Param("provider") String provider,
            @Param("accountEmail") String accountEmail,
            @Param("externalMessageId") String externalMessageId
    );

    long countByMailAccountUserId(String userId);

    long countByMailAccountUserIdAndIsReadFalse(String userId);

    List<EmailMessage> findByMailAccount_IdAndReceivedAtGreaterThanEqualAndReceivedAtBeforeOrderByReceivedAtDesc(
            String mailAccountId,
            LocalDateTime periodStartInclusive,
            LocalDateTime periodEndExclusive
    );
}
