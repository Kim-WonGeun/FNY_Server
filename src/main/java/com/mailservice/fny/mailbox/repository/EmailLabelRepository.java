package com.mailservice.fny.mailbox.repository;

import com.mailservice.fny.mailbox.entity.EmailLabel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLabelRepository extends JpaRepository<EmailLabel, String> {

    List<EmailLabel> findByEmailIdOrderByCreatedAtAsc(String emailId);
}
