package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.MailAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailAccountRepository extends JpaRepository<MailAccount, String> {

    List<MailAccount> findByUserIdOrderByIsPrimaryDescCreatedAtAsc(String userId);

    Optional<MailAccount> findByProviderAndProviderAccountId(String provider, String providerAccountId);

    long countByUserId(String userId);
}
