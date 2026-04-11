package com.mailservice.fny.integration.mail.repository;

import com.mailservice.fny.integration.mail.entity.MailIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailIntegrationRepository extends JpaRepository<MailIntegration, Long> {
}
