package com.mailservice.fny.integration.mail.infrastructure;

import com.mailservice.fny.integration.mail.entity.MailDelivery;
import com.mailservice.fny.integration.mail.entity.MailIntegration;
import com.mailservice.fny.integration.mail.entity.MailProviderType;
import com.mailservice.fny.integration.mail.entity.MailSendResult;
import com.mailservice.fny.integration.mail.entity.MailSenderGateway;
import com.mailservice.fny.integration.mail.exception.MailDeliveryException;
import jakarta.mail.internet.InternetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailSenderGateway implements MailSenderGateway {

    @Override
    public boolean supports(MailProviderType providerType) {
        return providerType == MailProviderType.SMTP;
    }

    @Override
    public MailSendResult send(MailIntegration integration, MailDelivery delivery) {
        try {
            JavaMailSenderImpl mailSender = buildMailSender(integration);
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(integration.getEmailAddress(), integration.getDisplayName()));
            helper.setTo(delivery.to());
            helper.setSubject(delivery.subject());
            helper.setText(delivery.body(), delivery.html());

            mailSender.send(mimeMessage);

            return new MailSendResult(mimeMessage.getMessageID(), delivery.to(), integration.getProviderType());
        } catch (Exception exception) {
            throw new MailDeliveryException("메일 발송에 실패했습니다.", exception);
        }
    }

    private JavaMailSenderImpl buildMailSender(MailIntegration integration) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(integration.getSmtpHost());
        mailSender.setPort(integration.getSmtpPort());
        mailSender.setUsername(integration.getSmtpUsername());
        mailSender.setPassword(integration.getSmtpPassword());
        mailSender.setProtocol(integration.getProtocol());
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", Boolean.toString(integration.isTlsEnabled()));
        properties.put("mail.smtp.connectiontimeout", "5000");
        properties.put("mail.smtp.timeout", "5000");
        properties.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }
}
