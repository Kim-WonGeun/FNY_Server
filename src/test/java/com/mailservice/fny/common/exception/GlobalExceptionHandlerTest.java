package com.mailservice.fny.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.mailservice.fny.integration.gmail.GmailClientException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void gmailScopeFailureReturnsStableCode() {
        var response = handler.handleGmailFailure(new GmailClientException(
                403,
                """
                        {"error":{"status":"PERMISSION_DENIED","details":[{"reason":"ACCESS_TOKEN_SCOPE_INSUFFICIENT"}]}}
                        """
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GMAIL_SCOPE_INSUFFICIENT");
    }

    @Test
    void gmailInvalidTokenReturnsUnauthorizedCode() {
        var response = handler.handleGmailFailure(new GmailClientException(
                401,
                """
                        {"error":{"message":"Invalid Credentials","status":"UNAUTHENTICATED"}}
                        """
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GMAIL_TOKEN_INVALID");
    }

    @Test
    void gmailRateLimitReturnsTooManyRequestsCode() {
        var response = handler.handleGmailFailure(new GmailClientException(
                429,
                """
                        {"error":{"status":"RESOURCE_EXHAUSTED","reason":"rateLimitExceeded"}}
                        """
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GMAIL_RATE_LIMITED");
    }

    @Test
    void gmailDisabledApiReturnsServiceDisabledCode() {
        var response = handler.handleGmailFailure(new GmailClientException(
                403,
                """
                        {"error":{"reason":"SERVICE_DISABLED","message":"Gmail API has not been used"}}
                        """
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GMAIL_SERVICE_DISABLED");
    }

    @Test
    void gmailTemporaryFailureReturnsRetryableCode() {
        var response = handler.handleGmailFailure(new GmailClientException(503, "{}"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("GMAIL_TEMPORARY_FAILURE");
    }
}
