package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.EmailRecipient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRecipientRepository extends JpaRepository<EmailRecipient, String> {

    List<EmailRecipient> findByEmailIdOrderByRecipientTypeAscCreatedAtAsc(String emailId);
}
