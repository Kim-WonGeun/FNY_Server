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
        if (exception.isInsufficientScope()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(
                            "GMAIL_SCOPE_INSUFFICIENT",
                            "Gmail 읽기 권한이 부족합니다. Google 계정의 FNY-Service 앱 권한을 삭제한 뒤 다시 로그인해 주세요.",
                            LocalDateTime.now()
                    ));
        }
        if (exception.isInvalidToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiErrorResponse(
                            "GMAIL_TOKEN_INVALID",
                            "Google 로그인 토큰이 만료되었거나 유효하지 않습니다. 다시 로그인해 주세요.",
                            LocalDateTime.now()
                    ));
        }
        if (exception.isServiceDisabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(
                            "GMAIL_SERVICE_DISABLED",
                            "Google Cloud 프로젝트에서 Gmail API가 비활성화되어 있습니다. Gmail API를 사용 설정한 뒤 다시 시도해 주세요.",
                            LocalDateTime.now()
                    ));
        }
        if (exception.isRateLimited()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiErrorResponse(
                            "GMAIL_RATE_LIMITED",
                            "Gmail API 요청 한도에 도달했습니다. 잠시 후 다시 시도해 주세요.",
                            LocalDateTime.now()
                    ));
        }
        if (exception.isTemporaryFailure()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiErrorResponse(
                            "GMAIL_TEMPORARY_FAILURE",
                            "Gmail API가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요.",
                            LocalDateTime.now()
                    ));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorResponse(
                        "GMAIL_API_FAILED",
                        "Gmail API 요청에 실패했습니다. 잠시 후 다시 시도해 주세요.",
                        LocalDateTime.now()
                ));
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
