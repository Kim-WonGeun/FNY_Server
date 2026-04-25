package com.mailservice.fny.mailbox.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailboxDeduplicationService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MailboxDeduplicationService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int deduplicateUserEmails(String userId) {
        List<String> duplicateEmailIds = jdbcTemplate.queryForList("""
                select id
                from (
                    select
                        e.id,
                        row_number() over (
                            partition by
                                ma.user_id,
                                upper(ma.provider),
                                lower(ma.account_email),
                                e.external_message_id
                            order by e.received_at desc, e.created_at asc, e.id asc
                        ) as duplicate_rank
                    from emails e
                    join mail_accounts ma on ma.id = e.mail_account_id
                    where ma.user_id = :userId
                      and e.external_message_id is not null
                ) ranked
                where duplicate_rank > 1
                """, Map.of("userId", userId), String.class);

        if (duplicateEmailIds.isEmpty()) {
            return 0;
        }

        MapSqlParameterSource params = new MapSqlParameterSource("emailIds", duplicateEmailIds);
        jdbcTemplate.update("""
                delete from email_action_items
                where analysis_id in (
                    select id from email_analysis where email_id in (:emailIds)
                )
                """, params);
        jdbcTemplate.update("delete from email_labels where email_id in (:emailIds)", params);
        jdbcTemplate.update("delete from analysis_jobs where email_id in (:emailIds)", params);
        jdbcTemplate.update("delete from email_recipients where email_id in (:emailIds)", params);
        jdbcTemplate.update("delete from email_analysis where email_id in (:emailIds)", params);
        jdbcTemplate.update("delete from emails where id in (:emailIds)", params);
        return duplicateEmailIds.size();
    }
}
