package com.mailservice.fny.integration.mail.exception;

public class MailIntegrationNotFoundException extends RuntimeException {

    public MailIntegrationNotFoundException(Long integrationId) {
        super("메일 연동 정보를 찾을 수 없습니다. id=" + integrationId);
    }
}
