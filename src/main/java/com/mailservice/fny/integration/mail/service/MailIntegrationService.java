package com.mailservice.fny.integration.mail.service;

import com.mailservice.fny.integration.mail.dto.MailIntegrationCreateRequest;
import com.mailservice.fny.integration.mail.dto.MailIntegrationResponse;
import com.mailservice.fny.integration.mail.dto.MailSendRequest;
import com.mailservice.fny.integration.mail.dto.MailSendResponse;
import com.mailservice.fny.integration.mail.entity.MailDelivery;
import com.mailservice.fny.integration.mail.entity.MailIntegration;
import com.mailservice.fny.integration.mail.entity.MailSendResult;
import com.mailservice.fny.integration.mail.entity.MailSenderGateway;
import com.mailservice.fny.integration.mail.exception.MailIntegrationNotFoundException;
import com.mailservice.fny.integration.mail.repository.MailIntegrationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MailIntegrationService {

    private final MailIntegrationRepository mailIntegrationRepository;
    private final List<MailSenderGateway> mailSenderGateways;

    public MailIntegrationService(
            MailIntegrationRepository mailIntegrationRepository,
            List<MailSenderGateway> mailSenderGateways
    ) {
        this.mailIntegrationRepository = mailIntegrationRepository;
        this.mailSenderGateways = mailSenderGateways;
    }

    public MailIntegrationResponse createIntegration(MailIntegrationCreateRequest request) {
        MailIntegration integration = new MailIntegration(
                request.providerType(),
                request.displayName(),
                request.emailAddress(),
                request.smtpHost(),
                request.smtpPort(),
                request.smtpUsername(),
                request.smtpPassword(),
                request.protocol(),
                request.tlsEnabled()
        );

        return MailIntegrationResponse.from(mailIntegrationRepository.save(integration));
    }

    @Transactional(readOnly = true)
    public List<MailIntegrationResponse> getIntegrations() {
        return mailIntegrationRepository.findAll().stream()
                .map(MailIntegrationResponse::from)
                .toList();
    }

    public MailSendResponse sendMail(MailSendRequest request) {
        MailIntegration integration = mailIntegrationRepository.findById(request.integrationId())
                .orElseThrow(() -> new MailIntegrationNotFoundException(request.integrationId()));

        MailSenderGateway gateway = mailSenderGateways.stream()
                .filter(candidate -> candidate.supports(integration.getProviderType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "지원되지 않는 메일 제공자입니다. provider=" + integration.getProviderType()));

        MailSendResult result = gateway.send(
                integration,
                new MailDelivery(request.to(), request.subject(), request.body(), request.html())
        );

        return MailSendResponse.from(result);
    }
}
