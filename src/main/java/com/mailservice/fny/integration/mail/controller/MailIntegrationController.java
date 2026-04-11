package com.mailservice.fny.integration.mail.controller;

import com.mailservice.fny.integration.mail.dto.MailIntegrationCreateRequest;
import com.mailservice.fny.integration.mail.dto.MailIntegrationResponse;
import com.mailservice.fny.integration.mail.dto.MailSendRequest;
import com.mailservice.fny.integration.mail.dto.MailSendResponse;
import com.mailservice.fny.integration.mail.service.MailIntegrationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
public class MailIntegrationController {

    private final MailIntegrationService mailIntegrationService;

    public MailIntegrationController(MailIntegrationService mailIntegrationService) {
        this.mailIntegrationService = mailIntegrationService;
    }

    @PostMapping("/integrations")
    @ResponseStatus(HttpStatus.CREATED)
    public MailIntegrationResponse createIntegration(@Valid @RequestBody MailIntegrationCreateRequest request) {
        return mailIntegrationService.createIntegration(request);
    }

    @GetMapping("/integrations")
    public List<MailIntegrationResponse> getIntegrations() {
        return mailIntegrationService.getIntegrations();
    }

    @PostMapping("/send")
    public MailSendResponse sendMail(@Valid @RequestBody MailSendRequest request) {
        return mailIntegrationService.sendMail(request);
    }
}
