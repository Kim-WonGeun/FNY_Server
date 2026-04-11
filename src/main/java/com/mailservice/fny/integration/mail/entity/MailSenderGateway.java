package com.mailservice.fny.integration.mail.entity;

public interface MailSenderGateway {

    boolean supports(MailProviderType providerType);

    MailSendResult send(MailIntegration integration, MailDelivery delivery);
}
