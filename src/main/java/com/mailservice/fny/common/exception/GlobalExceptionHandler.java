package com.mailservice.fny.common.exception;

import com.mailservice.fny.analysis.exception.AnalysisJobNotFoundException;
import com.mailservice.fny.integration.gmail.GmailClientException;
import com.mailservice.fny.integration.mail.exception.MailDeliveryException;
import com.mailservice.fny.integration.mail.exception.MailIntegrationNotFoundException;
import com.mailservice.fny.mailbox.exception.MailboxNotFoundException;
import java.time.LocalDateTime;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final GmailApiErrorMapper gmailApiErrorMapper;

    public GlobalExceptionHandler(GmailApiErrorMapper gmailApiErrorMapper) {
        this.gmailApiErrorMapper = gmailApiErrorMapper;
    }

    @ExceptionHandler(MailIntegrationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(MailIntegrationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("MAIL_INTEGRATION_NOT_FOUND", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MailboxNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMailboxNotFound(MailboxNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("MAILBOX_RESOURCE_NOT_FOUND", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(AnalysisJobNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAnalysisJobNotFound(AnalysisJobNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("ANALYSIS_JOB_NOT_FOUND", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_REQUEST", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailure(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("AUTHENTICATION_REQUIRED", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MailDeliveryException.class)
    public ResponseEntity<ApiErrorResponse> handleDeliveryFailure(MailDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorResponse("MAIL_DELIVERY_FAILED", exception.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(GmailClientException.class)
    public ResponseEntity<ApiErrorResponse> handleGmailFailure(GmailClientException exception) {
        GmailApiErrorMapper.MappedError error = gmailApiErrorMapper.map(exception);
        return ResponseEntity.status(error.status())
                .body(new ApiErrorResponse(error.code(), error.message(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("VALIDATION_ERROR", message, LocalDateTime.now()));
    }
}
