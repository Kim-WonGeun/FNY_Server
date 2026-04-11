package com.mailservice.fny.integration.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mailservice.fny.integration.mail.dto.MailIntegrationCreateRequest;
import com.mailservice.fny.integration.mail.dto.MailSendRequest;
import com.mailservice.fny.integration.mail.entity.MailIntegration;
import com.mailservice.fny.integration.mail.entity.MailProviderType;
import com.mailservice.fny.integration.mail.entity.MailSendResult;
import com.mailservice.fny.integration.mail.entity.MailSenderGateway;
import com.mailservice.fny.integration.mail.exception.MailIntegrationNotFoundException;
import com.mailservice.fny.integration.mail.repository.MailIntegrationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailIntegrationServiceTest {

    @Mock
    private MailIntegrationRepository mailIntegrationRepository;

    @Mock
    private MailSenderGateway mailSenderGateway;

    private MailIntegrationService mailIntegrationService;

    @BeforeEach
    void setUp() {
        mailIntegrationService = new MailIntegrationService(mailIntegrationRepository, List.of(mailSenderGateway));
    }

    @Test
    void createIntegrationStoresIntegration() {
        MailIntegrationCreateRequest request = new MailIntegrationCreateRequest(
                MailProviderType.SMTP,
                "FNY",
                "sender@example.com",
                "smtp.example.com",
                587,
                "sender@example.com",
                "secret",
                "smtp",
                true
        );

        when(mailIntegrationRepository.save(any(MailIntegration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = mailIntegrationService.createIntegration(request);

        assertThat(response.providerType()).isEqualTo(MailProviderType.SMTP);
        assertThat(response.smtpHost()).isEqualTo("smtp.example.com");
    }

    @Test
    void sendMailDelegatesToSupportedGateway() {
        MailIntegration integration = new MailIntegration(
                MailProviderType.SMTP,
                "FNY",
                "sender@example.com",
                "smtp.example.com",
                587,
                "sender@example.com",
                "secret",
                "smtp",
                true
        );

        when(mailIntegrationRepository.findById(1L)).thenReturn(Optional.of(integration));
        when(mailSenderGateway.supports(MailProviderType.SMTP)).thenReturn(true);
        when(mailSenderGateway.send(any(MailIntegration.class), any()))
                .thenReturn(new MailSendResult("message-1", "user@example.com", MailProviderType.SMTP));

        var response = mailIntegrationService.sendMail(
                new MailSendRequest(1L, "user@example.com", "hello", "world", false)
        );

        assertThat(response.status()).isEqualTo("SENT");
        assertThat(response.recipient()).isEqualTo("user@example.com");
    }

    @Test
    void sendMailThrowsWhenIntegrationDoesNotExist() {
        when(mailIntegrationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mailIntegrationService.sendMail(
                new MailSendRequest(99L, "user@example.com", "hello", "world", false)
        )).isInstanceOf(MailIntegrationNotFoundException.class);
    }
}
